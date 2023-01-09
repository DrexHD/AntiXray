package me.drex.antixray.util.controller;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObfuscateChunkPacketBlockController extends ChunkPacketBlockControllerAntiXray {

    private final BlockState[] presetBlockStates;
    private final int[] presetBlockStateBitsGlobal;

    public ObfuscateChunkPacketBlockController(Level level, Executor executor, Set<Block> replacementBlocks, Set<Block> hiddenBlocks, int maxBlockHeight, int updateRadius, boolean lavaObscures) {
        super(level, executor, Stream.concat(replacementBlocks.stream(), hiddenBlocks.stream()).collect(Collectors.toSet()), maxBlockHeight, updateRadius, lavaObscures);

        Set<BlockState> presetBlockStateSet = new LinkedHashSet<>();
        for (Block block : hiddenBlocks) {
            if (block != null && !(block instanceof EntityBlock)) {
                presetBlockStateSet.add(block.defaultBlockState());
            }
        }

        presetBlockStates = presetBlockStateSet.isEmpty() ? new BlockState[]{Blocks.DIAMOND_ORE.defaultBlockState()} : presetBlockStateSet.toArray(new BlockState[]{});
        presetBlockStateBitsGlobal = new int[presetBlockStates.length];

        for (int i = 0; i < presetBlockStates.length; i++) {
            presetBlockStateBitsGlobal[i] = GLOBAL_BLOCKSTATE_PALETTE.idFor(presetBlockStates[i]);
        }
    }

    @Override
    protected int getPresetBlockStatesLength() {
        return presetBlockStates.length;
    }

    @Override
    protected int[] getPresetBlockStateBits(Level level, int bottomBlockY) {
        return presetBlockStateBitsGlobal;
    }

    @Override
    public BlockState[] getPresetBlockStates(Level level, int bottomBlockY) {
        if (bottomBlockY < maxBlockHeight) {
            return presetBlockStates;
        }
        return null;
    }

}
