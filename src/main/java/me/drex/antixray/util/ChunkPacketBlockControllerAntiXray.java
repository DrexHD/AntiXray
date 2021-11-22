package me.drex.antixray.util;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import me.drex.antixray.AntiXray;
import me.drex.antixray.config.WorldConfig;
import me.drex.antixray.mixin.accessor.ServerPlayerGameModeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

public final class ChunkPacketBlockControllerAntiXray extends ChunkPacketBlockController {

    private static final ThreadLocal<boolean[]> solid = ThreadLocal.withInitial(() -> new boolean[Block.BLOCK_STATE_REGISTRY.size()]);
    private static final ThreadLocal<boolean[]> obfuscate = ThreadLocal.withInitial(() -> new boolean[Block.BLOCK_STATE_REGISTRY.size()]);
    // These boolean arrays represent chunk layers, true means don't obfuscate, false means obfuscate
    private static final ThreadLocal<boolean[][]> current = ThreadLocal.withInitial(() -> new boolean[16][16]);
    private static final ThreadLocal<boolean[][]> next = ThreadLocal.withInitial(() -> new boolean[16][16]);
    private static final ThreadLocal<boolean[][]> nextNext = ThreadLocal.withInitial(() -> new boolean[16][16]);
    private final Executor executor;
    private final EngineMode engineMode;
    private final int maxBlockHeight;
    private final int updateRadius;
    private final GlobalPalette<BlockState> globalPalette;
    private final BlockState[] presetBlockStates;
    private final BlockState[] presetBlockStatesFull;
    private final BlockState[] presetBlockStatesStone;
    private final BlockState[] presetBlockStatesNetherrack;
    private final BlockState[] presetBlockStatesEndStone;
    private final int[] presetBlockStateBitsGlobal;
    private final int[] presetBlockStateBitsStoneGlobal;
    private final int[] presetBlockStateBitsNetherrackGlobal;
    private final int[] presetBlockStateBitsEndStoneGlobal;
    private final Object2BooleanOpenHashMap<BlockState> solidGlobal = new Object2BooleanOpenHashMap<>(Block.BLOCK_STATE_REGISTRY.size());
    private final Object2BooleanOpenHashMap<BlockState> obfuscateGlobal = new Object2BooleanOpenHashMap<>(Block.BLOCK_STATE_REGISTRY.size());
    private final LevelChunkSection[] emptyNearbyChunkSections = new LevelChunkSection[4];
    private final int maxBlockHeightUpdatePosition;
    // Actually these fields should be variables inside the obfuscate method but in sync mode or with SingleThreadExecutor in async mode it's okay (even without ThreadLocal)
    // If an ExecutorService with multiple threads is used, ThreadLocal must be used here
    private final ThreadLocal<int[]> presetBlockStateBits = ThreadLocal.withInitial(() -> new int[getPresetBlockStatesFullLength()]);

    public ChunkPacketBlockControllerAntiXray(Level level, Executor executor) {
        this.globalPalette = new GlobalPalette<>(Block.BLOCK_STATE_REGISTRY);
        this.executor = executor;
        WorldConfig worldConfig = ((LevelInterface) level).getWorldConfig();
        engineMode = worldConfig.engineMode;
        maxBlockHeight = worldConfig.maxBlockHeight >> 4 << 4;
        updateRadius = worldConfig.updateRadius;
        List<String> toObfuscate;

        if (engineMode == EngineMode.HIDE) {
            toObfuscate = worldConfig.hiddenBlocks;
            presetBlockStates = null;
            presetBlockStatesFull = null;
            presetBlockStatesStone = new BlockState[]{Blocks.STONE.defaultBlockState()};
            presetBlockStatesNetherrack = new BlockState[]{Blocks.NETHERRACK.defaultBlockState()};
            presetBlockStatesEndStone = new BlockState[]{Blocks.END_STONE.defaultBlockState()};
            presetBlockStateBitsGlobal = null;
            presetBlockStateBitsStoneGlobal = new int[]{globalPalette.idFor(Blocks.STONE.defaultBlockState())};
            presetBlockStateBitsNetherrackGlobal = new int[]{globalPalette.idFor(Blocks.NETHERRACK.defaultBlockState())};
            presetBlockStateBitsEndStoneGlobal = new int[]{globalPalette.idFor(Blocks.END_STONE.defaultBlockState())};
        } else {
            toObfuscate = worldConfig.replacementBlocks;
            List<BlockState> presetBlockStateList = new LinkedList<>();

            for (String id : worldConfig.hiddenBlocks) {
                Block block = Registry.BLOCK.getOptional(new ResourceLocation(id)).orElse(null);

                if (block != null && !(block instanceof EntityBlock)) {
                    toObfuscate.add(id);
                    presetBlockStateList.add(block.defaultBlockState());
                }
            }

            Set<BlockState> presetBlockStateSet = new LinkedHashSet<>(presetBlockStateList);
            presetBlockStates = presetBlockStateSet.isEmpty() ? new BlockState[]{Blocks.DIAMOND_ORE.defaultBlockState()} : presetBlockStateSet.toArray(new BlockState[0]);
            presetBlockStatesFull = presetBlockStateSet.isEmpty() ? new BlockState[]{Blocks.DIAMOND_ORE.defaultBlockState()} : presetBlockStateList.toArray(new BlockState[0]);
            presetBlockStatesStone = null;
            presetBlockStatesNetherrack = null;
            presetBlockStatesEndStone = null;
            presetBlockStateBitsGlobal = new int[presetBlockStatesFull.length];

            for (int i = 0; i < presetBlockStatesFull.length; i++) {
                presetBlockStateBitsGlobal[i] = this.globalPalette.idFor(presetBlockStatesFull[i]);
            }

            presetBlockStateBitsStoneGlobal = null;
            presetBlockStateBitsNetherrackGlobal = null;
            presetBlockStateBitsEndStoneGlobal = null;
        }

        for (String id : toObfuscate) {
            Block block = Registry.BLOCK.getOptional(new ResourceLocation(id)).orElse(null);

            // Don't obfuscate air because air causes unnecessary block updates and causes block updates to fail in the void
            if (block != null && !block.defaultBlockState().isAir()) {
                // Replace all block states of a specified block
                for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
                    obfuscateGlobal.put(blockState, true);
                }
            }
        }

        EmptyLevelChunk emptyChunk = new EmptyLevelChunk(level, new ChunkPos(0, 0));
        BlockPos zeroPos = new BlockPos(0, 0, 0);

        Block.BLOCK_STATE_REGISTRY.iterator().forEachRemaining((blockState) -> {
                    solidGlobal.put(blockState, blockState.isRedstoneConductor(emptyChunk, zeroPos)
                            && blockState.getBlock() != Blocks.SPAWNER && blockState.getBlock() != Blocks.BARRIER && blockState.getBlock() != Blocks.SHULKER_BOX && blockState.getBlock() != Blocks.SLIME_BLOCK || worldConfig.lavaObscures && blockState == Blocks.LAVA.defaultBlockState());
                    // Comparing blockState == Blocks.LAVA.defaultBlockState() instead of blockState.getBlock() == Blocks.LAVA ensures that only "stationary lava" is used
                    // shulker box checks TE.
                }
        );

        maxBlockHeightUpdatePosition = maxBlockHeight + updateRadius - 1;
    }

    private int getPresetBlockStatesFullLength() {
        return engineMode == EngineMode.HIDE ? 1 : presetBlockStatesFull.length;
    }

    private boolean isSolid(BlockState blockState) {
        return solidGlobal.getOrDefault(blockState, false);
    }

    @Override
    public BlockState[] getPresetBlockStates(Level level, LevelChunkSection chunkSection) {
        // Return the block states to be added to the paletted containers so that they can be used for obfuscation
        if (chunkSection.bottomBlockY() < maxBlockHeight && !chunkSection.hasOnlyAir()) {
            if (engineMode == EngineMode.HIDE) {
                ResourceKey<Level> resourceKey = level.dimension();
                if (resourceKey.equals(Level.NETHER)) {
                    return presetBlockStatesNetherrack;
                } else if (resourceKey.equals(Level.END)) {
                    return presetBlockStatesEndStone;
                } else {
                    return new BlockState[]{chunkSection.bottomBlockY() >= 0 ? Blocks.STONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState()};
                }
            }
            return presetBlockStates;
        }

        return null;
    }

    @Override
    public ChunkPacketInfoAntiXray getChunkPacketInfo(ClientboundLevelChunkPacketData chunkPacketData, LevelChunk chunk) {
        // Return a new instance to collect data and objects in the right state while creating the chunk packet for thread safe access later
        return new ChunkPacketInfoAntiXray(chunkPacketData, chunk, this);
    }

    @Override
    public void modifyBlocks(ClientboundLevelChunkPacketData chunkPacketData, ChunkPacketInfo<BlockState> chunkPacketInfo) {
        if (!(chunkPacketInfo instanceof ChunkPacketInfoAntiXray)) {
            ((ClientboundLevelChunkPacketDataInterface) chunkPacketData).setReady();
            return;
        }

        if (!AntiXray.getMinecraftServer().isSameThread()) {
            AntiXray.getMinecraftServer().execute(() -> modifyBlocks(chunkPacketData, chunkPacketInfo));
        }

        LevelChunk chunk = chunkPacketInfo.getChunk();
        int x = chunk.getPos().x;
        int z = chunk.getPos().z;
        ServerLevelInterface level = (ServerLevelInterface) chunk.getLevel();
        ((ChunkPacketInfoAntiXray) chunkPacketInfo).setNearbyChunks(level.getChunkIfLoaded(x - 1, z), level.getChunkIfLoaded(x + 1, z), level.getChunkIfLoaded(x, z - 1), level.getChunkIfLoaded(x, z + 1));
        executor.execute((Runnable) chunkPacketInfo);
    }

    public void obfuscate(ChunkPacketInfoAntiXray chunkPacketInfoAntiXray) {
        int[] presetBlockStateBits = this.presetBlockStateBits.get();
        boolean[] solid = ChunkPacketBlockControllerAntiXray.solid.get();
        boolean[] obfuscate = ChunkPacketBlockControllerAntiXray.obfuscate.get();
        boolean[][] current = ChunkPacketBlockControllerAntiXray.current.get();
        boolean[][] next = ChunkPacketBlockControllerAntiXray.next.get();
        boolean[][] nextNext = ChunkPacketBlockControllerAntiXray.nextNext.get();
        // bitStorageReader, bitStorageWriter and nearbyChunkSections could also be reused (with ThreadLocal if necessary) but it's not worth it
        BitStorageReader bitStorageReader = new BitStorageReader();
        BitStorageWriter bitStorageWriter = new BitStorageWriter();
        LevelChunkSection[] nearbyChunkSections = new LevelChunkSection[4];
        LevelChunk chunk = chunkPacketInfoAntiXray.getChunk();
        Level level = chunk.getLevel();
        int maxChunkSectionIndex = Math.min((maxBlockHeight >> 4) - chunk.getMinSection(), chunk.getSectionsCount() - 1);
        boolean[] solidTemp = null;
        boolean[] obfuscateTemp = null;
        bitStorageReader.setBuffer(chunkPacketInfoAntiXray.getBuffer());
        bitStorageWriter.setBuffer(chunkPacketInfoAntiXray.getBuffer());
        int numberOfBlocks = presetBlockStateBits.length;
        // Keep the lambda expressions as simple as possible. They are used very frequently.
        IntSupplier random = numberOfBlocks == 1 ? (() -> 0) : new IntSupplier() {
            private int state;

            {
                while ((state = ThreadLocalRandom.current().nextInt()) == 0) ;
            }

            @Override
            public int getAsInt() {
                // https://en.wikipedia.org/wiki/Xorshift
                state ^= state << 13;
                state ^= state >>> 17;
                state ^= state << 5;
                // https://www.pcg-random.org/posts/bounded-rands.html
                return (int) ((Integer.toUnsignedLong(state) * numberOfBlocks) >>> 32);
            }
        };

        for (int chunkSectionIndex = 0; chunkSectionIndex <= maxChunkSectionIndex; chunkSectionIndex++) {
            if (chunkPacketInfoAntiXray.isWritten(chunkSectionIndex) && chunkPacketInfoAntiXray.getPresetValues(chunkSectionIndex) != null) {
                int[] presetBlockStateBitsTemp;

                if (chunkPacketInfoAntiXray.getPalette(chunkSectionIndex) == this.globalPalette) {
                    if (engineMode == EngineMode.HIDE) {
                        ResourceKey<Level> resourceKey = level.dimension();
                        if (resourceKey.equals(Level.NETHER)) {
                            presetBlockStateBitsTemp = presetBlockStateBitsNetherrackGlobal;
                        } else if (resourceKey.equals(Level.END)) {
                            presetBlockStateBitsTemp = presetBlockStateBitsEndStoneGlobal;
                        } else {
                            presetBlockStateBitsTemp = presetBlockStateBitsStoneGlobal;
                        }
                    } else {
                        presetBlockStateBitsTemp = presetBlockStateBitsGlobal;
                    }
                } else {
                    // If it's presetBlockStates, use this.presetBlockStatesFull instead
                    BlockState[] presetBlockStatesFull = chunkPacketInfoAntiXray.getPresetValues(chunkSectionIndex) == presetBlockStates ? this.presetBlockStatesFull : chunkPacketInfoAntiXray.getPresetValues(chunkSectionIndex);
                    presetBlockStateBitsTemp = presetBlockStateBits;

                    for (int i = 0; i < presetBlockStateBitsTemp.length; i++) {
                        presetBlockStateBitsTemp[i] = chunkPacketInfoAntiXray.getPalette(chunkSectionIndex).idFor(presetBlockStatesFull[i]);
                    }
                }

                bitStorageWriter.setIndex(chunkPacketInfoAntiXray.getIndex(chunkSectionIndex));

                // Check if the chunk section below was not obfuscated
                if (chunkSectionIndex == 0 || !chunkPacketInfoAntiXray.isWritten(chunkSectionIndex - 1) || chunkPacketInfoAntiXray.getPresetValues(chunkSectionIndex - 1) == null) {
                    // If so, initialize some stuff
                    bitStorageReader.setBits(chunkPacketInfoAntiXray.getBits(chunkSectionIndex));
                    bitStorageReader.setIndex(chunkPacketInfoAntiXray.getIndex(chunkSectionIndex));
                    solidTemp = readPalette(chunkPacketInfoAntiXray.getPalette(chunkSectionIndex), solid, solidGlobal);
                    obfuscateTemp = readPalette(chunkPacketInfoAntiXray.getPalette(chunkSectionIndex), obfuscate, obfuscateGlobal);
                    // Read the blocks of the upper layer of the chunk section below if it exists
                    LevelChunkSection belowChunkSection = null;
                    boolean skipFirstLayer = chunkSectionIndex == 0 || (belowChunkSection = chunk.getSections()[chunkSectionIndex - 1]) == null;

                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            current[z][x] = true;
                            next[z][x] = skipFirstLayer || !isSolid(belowChunkSection.getBlockState(x, 15, z));
                        }
                    }

                    // Abuse the obfuscateLayer method to read the blocks of the first layer of the current chunk section
                    bitStorageWriter.setBits(0);
                    obfuscateLayer(-1, bitStorageReader, bitStorageWriter, solidTemp, obfuscateTemp, presetBlockStateBitsTemp, current, next, nextNext, emptyNearbyChunkSections, random);
                }

                bitStorageWriter.setBits(chunkPacketInfoAntiXray.getBits(chunkSectionIndex));
                nearbyChunkSections[0] = chunkPacketInfoAntiXray.getNearbyChunks()[0] == null ? null : chunkPacketInfoAntiXray.getNearbyChunks()[0].getSections()[chunkSectionIndex];
                nearbyChunkSections[1] = chunkPacketInfoAntiXray.getNearbyChunks()[1] == null ? null : chunkPacketInfoAntiXray.getNearbyChunks()[1].getSections()[chunkSectionIndex];
                nearbyChunkSections[2] = chunkPacketInfoAntiXray.getNearbyChunks()[2] == null ? null : chunkPacketInfoAntiXray.getNearbyChunks()[2].getSections()[chunkSectionIndex];
                nearbyChunkSections[3] = chunkPacketInfoAntiXray.getNearbyChunks()[3] == null ? null : chunkPacketInfoAntiXray.getNearbyChunks()[3].getSections()[chunkSectionIndex];

                // Obfuscate all layers of the current chunk section except the upper one
                for (int y = 0; y < 15; y++) {
                    boolean[][] temp = current;
                    current = next;
                    next = nextNext;
                    nextNext = temp;
                    obfuscateLayer(y, bitStorageReader, bitStorageWriter, solidTemp, obfuscateTemp, presetBlockStateBitsTemp, current, next, nextNext, nearbyChunkSections, random);
                }

                // Check if the chunk section above doesn't need obfuscation
                if (chunkSectionIndex == maxChunkSectionIndex || !chunkPacketInfoAntiXray.isWritten(chunkSectionIndex + 1) || chunkPacketInfoAntiXray.getPresetValues(chunkSectionIndex + 1) == null) {
                    // If so, obfuscate the upper layer of the current chunk section by reading blocks of the first layer from the chunk section above if it exists
                    LevelChunkSection aboveChunkSection;

                    if (chunkSectionIndex != chunk.getSectionsCount() - 1 && (aboveChunkSection = chunk.getSections()[chunkSectionIndex + 1]) != null) {
                        boolean[][] temp = current;
                        current = next;
                        next = nextNext;
                        nextNext = temp;

                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                if (!isSolid(aboveChunkSection.getBlockState(x, 0, z))) {
                                    current[z][x] = true;
                                }
                            }
                        }

                        // There is nothing to read anymore
                        bitStorageReader.setBits(0);
                        solid[0] = true;
                        obfuscateLayer(15, bitStorageReader, bitStorageWriter, solid, obfuscateTemp, presetBlockStateBitsTemp, current, next, nextNext, nearbyChunkSections, random);
                    }
                } else {
                    // If not, initialize the reader and other stuff for the chunk section above to obfuscate the upper layer of the current chunk section
                    bitStorageReader.setBits(chunkPacketInfoAntiXray.getBits(chunkSectionIndex + 1));
                    bitStorageReader.setIndex(chunkPacketInfoAntiXray.getIndex(chunkSectionIndex + 1));
                    solidTemp = readPalette(chunkPacketInfoAntiXray.getPalette(chunkSectionIndex + 1), solid, solidGlobal);
                    obfuscateTemp = readPalette(chunkPacketInfoAntiXray.getPalette(chunkSectionIndex + 1), obfuscate, obfuscateGlobal);
                    boolean[][] temp = current;
                    current = next;
                    next = nextNext;
                    nextNext = temp;
                    obfuscateLayer(15, bitStorageReader, bitStorageWriter, solidTemp, obfuscateTemp, presetBlockStateBitsTemp, current, next, nextNext, nearbyChunkSections, random);
                }

                bitStorageWriter.flush();
            }
        }

        ((ClientboundLevelChunkPacketDataInterface) chunkPacketInfoAntiXray.getChunkPacketData()).setReady();
    }

    private void obfuscateLayer(int y, BitStorageReader bitStorageReader, BitStorageWriter bitStorageWriter, boolean[] solid, boolean[] obfuscate, int[] presetBlockStateBits, boolean[][] current, boolean[][] next, boolean[][] nextNext, LevelChunkSection[] nearbyChunkSections, IntSupplier random) {
        // First block of first line
        int bits = bitStorageReader.read();

        if (nextNext[0][0] = !solid[bits]) {
            bitStorageWriter.skip();
            next[0][1] = true;
            next[1][0] = true;
        } else {
            if (current[0][0] || nearbyChunkSections[2] == null || !isSolid(nearbyChunkSections[2].getBlockState(0, y, 15)) || nearbyChunkSections[0] == null || !isSolid(nearbyChunkSections[0].getBlockState(15, y, 0))) {
                bitStorageWriter.skip();
            } else {
                bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
            }
        }

        if (!obfuscate[bits]) {
            next[0][0] = true;
        }

        // First line
        for (int x = 1; x < 15; x++) {
            bits = bitStorageReader.read();

            if (nextNext[0][x] = !solid[bits]) {
                bitStorageWriter.skip();
                next[0][x - 1] = true;
                next[0][x + 1] = true;
                next[1][x] = true;
            } else {
                if (current[0][x] || nearbyChunkSections[2] == null || !isSolid(nearbyChunkSections[2].getBlockState(x, y, 15))) {
                    bitStorageWriter.skip();
                } else {
                    bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
                }
            }

            if (!obfuscate[bits]) {
                next[0][x] = true;
            }
        }

        // Last block of first line
        bits = bitStorageReader.read();

        if (nextNext[0][15] = !solid[bits]) {
            bitStorageWriter.skip();
            next[0][14] = true;
            next[1][15] = true;
        } else {
            if (current[0][15] || nearbyChunkSections[2] == null || !isSolid(nearbyChunkSections[2].getBlockState(15, y, 15)) || nearbyChunkSections[1] == null || !isSolid(nearbyChunkSections[1].getBlockState(0, y, 0))) {
                bitStorageWriter.skip();
            } else {
                bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
            }
        }

        if (!obfuscate[bits]) {
            next[0][15] = true;
        }

        // All inner lines
        for (int z = 1; z < 15; z++) {
            // First block
            bits = bitStorageReader.read();

            if (nextNext[z][0] = !solid[bits]) {
                bitStorageWriter.skip();
                next[z][1] = true;
                next[z - 1][0] = true;
                next[z + 1][0] = true;
            } else {
                if (current[z][0] || nearbyChunkSections[0] == null || !isSolid(nearbyChunkSections[0].getBlockState(15, y, z))) {
                    bitStorageWriter.skip();
                } else {
                    bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
                }
            }

            if (!obfuscate[bits]) {
                next[z][0] = true;
            }

            // All inner blocks
            for (int x = 1; x < 15; x++) {
                bits = bitStorageReader.read();

                if (nextNext[z][x] = !solid[bits]) {
                    bitStorageWriter.skip();
                    next[z][x - 1] = true;
                    next[z][x + 1] = true;
                    next[z - 1][x] = true;
                    next[z + 1][x] = true;
                } else {
                    if (current[z][x]) {
                        bitStorageWriter.skip();
                    } else {
                        bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
                    }
                }

                if (!obfuscate[bits]) {
                    next[z][x] = true;
                }
            }

            // Last block
            bits = bitStorageReader.read();

            if (nextNext[z][15] = !solid[bits]) {
                bitStorageWriter.skip();
                next[z][14] = true;
                next[z - 1][15] = true;
                next[z + 1][15] = true;
            } else {
                if (current[z][15] || nearbyChunkSections[1] == null || !isSolid(nearbyChunkSections[1].getBlockState(0, y, z))) {
                    bitStorageWriter.skip();
                } else {
                    bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
                }
            }

            if (!obfuscate[bits]) {
                next[z][15] = true;
            }
        }

        // First block of last line
        bits = bitStorageReader.read();

        if (nextNext[15][0] = !solid[bits]) {
            bitStorageWriter.skip();
            next[15][1] = true;
            next[14][0] = true;
        } else {
            if (current[15][0] || nearbyChunkSections[3] == null || !isSolid(nearbyChunkSections[3].getBlockState(0, y, 0)) || nearbyChunkSections[0] == null || !isSolid(nearbyChunkSections[0].getBlockState(15, y, 15))) {
                bitStorageWriter.skip();
            } else {
                bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
            }
        }

        if (!obfuscate[bits]) {
            next[15][0] = true;
        }

        // Last line
        for (int x = 1; x < 15; x++) {
            bits = bitStorageReader.read();

            if (nextNext[15][x] = !solid[bits]) {
                bitStorageWriter.skip();
                next[15][x - 1] = true;
                next[15][x + 1] = true;
                next[14][x] = true;
            } else {
                if (current[15][x] || nearbyChunkSections[3] == null || !isSolid(nearbyChunkSections[3].getBlockState(x, y, 0))) {
                    bitStorageWriter.skip();
                } else {
                    bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
                }
            }

            if (!obfuscate[bits]) {
                next[15][x] = true;
            }
        }

        // Last block of last line
        bits = bitStorageReader.read();

        if (nextNext[15][15] = !solid[bits]) {
            bitStorageWriter.skip();
            next[15][14] = true;
            next[14][15] = true;
        } else {
            if (current[15][15] || nearbyChunkSections[3] == null || !isSolid(nearbyChunkSections[3].getBlockState(15, y, 0)) || nearbyChunkSections[1] == null || !isSolid(nearbyChunkSections[1].getBlockState(0, y, 15))) {
                bitStorageWriter.skip();
            } else {
                bitStorageWriter.write(presetBlockStateBits[random.getAsInt()]);
            }
        }

        if (!obfuscate[bits]) {
            next[15][15] = true;
        }
    }

    private boolean[] readPalette(Palette<BlockState> palette, boolean[] temp, Object2BooleanOpenHashMap<BlockState> global) {
        for (int i = 0; i < palette.getSize(); i++) {
            temp[i] = global.getOrDefault(palette.valueFor(i), false);
        }
        return temp;
    }

    @Override
    public void onBlockChange(Level level, BlockPos blockPos, BlockState newBlockState, @Nullable BlockState oldBlockState) {
        if (oldBlockState != null && isSolid(oldBlockState) && !isSolid(newBlockState) && blockPos.getY() <= maxBlockHeightUpdatePosition) {
            updateNearbyBlocks(level, blockPos);
        }
    }

    @Override
    public void onPlayerLeftClickBlock(ServerPlayerGameMode serverPlayerGameMode, BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight) {
        if (blockPos.getY() <= maxBlockHeightUpdatePosition) {
            updateNearbyBlocks(((ServerPlayerGameModeAccessor) serverPlayerGameMode).getLevel(), blockPos);
        }
    }

    private void updateNearbyBlocks(Level level, BlockPos blockPos) {
        if (updateRadius >= 2) {
            BlockPos temp = blockPos.west();
            updateBlock(level, temp);
            updateBlock(level, temp.west());
            updateBlock(level, temp.below());
            updateBlock(level, temp.above());
            updateBlock(level, temp.north());
            updateBlock(level, temp.south());
            updateBlock(level, temp = blockPos.east());
            updateBlock(level, temp.east());
            updateBlock(level, temp.below());
            updateBlock(level, temp.above());
            updateBlock(level, temp.north());
            updateBlock(level, temp.south());
            updateBlock(level, temp = blockPos.below());
            updateBlock(level, temp.below());
            updateBlock(level, temp.north());
            updateBlock(level, temp.south());
            updateBlock(level, temp = blockPos.above());
            updateBlock(level, temp.above());
            updateBlock(level, temp.north());
            updateBlock(level, temp.south());
            updateBlock(level, temp = blockPos.north());
            updateBlock(level, temp.north());
            updateBlock(level, temp = blockPos.south());
            updateBlock(level, temp.south());
        } else if (updateRadius == 1) {
            updateBlock(level, blockPos.west());
            updateBlock(level, blockPos.east());
            updateBlock(level, blockPos.below());
            updateBlock(level, blockPos.above());
            updateBlock(level, blockPos.north());
            updateBlock(level, blockPos.south());
        } else {
            // Do nothing if updateRadius <= 0 (test mode)
        }
    }

    private void updateBlock(Level level, BlockPos blockPos) {
        BlockState blockState = level.getBlockState(blockPos);

        if (obfuscateGlobal.getOrDefault(blockState, false)) {
            ((ServerLevel) level).getChunkSource().blockChanged(blockPos);
        }
    }

    public enum EngineMode {

        HIDE(1, "hide ores"),
        OBFUSCATE(2, "obfuscate");

        private final int id;
        private final String description;

        EngineMode(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static EngineMode getById(int id) {
            for (EngineMode engineMode : values()) {
                if (engineMode.id == id) {
                    return engineMode;
                }
            }

            return null;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }
}
