package me.drex.antixray.util.controller;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.concurrent.Executor;

public class HideChunkPacketBlockController extends ChunkPacketBlockControllerAntiXray {

    private final BlockState[] presetBlockStatesStone;
    private final BlockState[] presetBlockStatesDeepslate;
    private final BlockState[] presetBlockStatesNetherrack;
    private final BlockState[] presetBlockStatesEndStone;
    private final int[] presetBlockStateBitsStoneGlobal;
    private final int[] presetBlockStateBitsDeepslateGlobal;
    private final int[] presetBlockStateBitsNetherrackGlobal;
    private final int[] presetBlockStateBitsEndStoneGlobal;

    public HideChunkPacketBlockController(Level level, Set<Block> toObfuscate, int maxBlockHeight, int updateRadius, boolean lavaObscures) {
        super(level, toObfuscate, maxBlockHeight, updateRadius, lavaObscures);
        presetBlockStatesStone = new BlockState[]{Blocks.STONE.defaultBlockState()};
        presetBlockStatesDeepslate = new BlockState[]{Blocks.DEEPSLATE.defaultBlockState()};
        presetBlockStatesNetherrack = new BlockState[]{Blocks.NETHERRACK.defaultBlockState()};
        presetBlockStatesEndStone = new BlockState[]{Blocks.END_STONE.defaultBlockState()};
        presetBlockStateBitsStoneGlobal = new int[]{GLOBAL_BLOCKSTATE_PALETTE.idFor(Blocks.STONE.defaultBlockState())};
        presetBlockStateBitsDeepslateGlobal = new int[]{GLOBAL_BLOCKSTATE_PALETTE.idFor(Blocks.DEEPSLATE.defaultBlockState())};
        presetBlockStateBitsNetherrackGlobal = new int[]{GLOBAL_BLOCKSTATE_PALETTE.idFor(Blocks.NETHERRACK.defaultBlockState())};
        presetBlockStateBitsEndStoneGlobal = new int[]{GLOBAL_BLOCKSTATE_PALETTE.idFor(Blocks.END_STONE.defaultBlockState())};
    }

    @Override
    protected int getPresetBlockStatesLength() {
        return 1;
    }

    @Override
    protected int[] getPresetBlockStateBits(Level level, int bottomBlockY) {
        if (level.dimension().equals(Level.NETHER)) {
            return presetBlockStateBitsNetherrackGlobal;
        } else if (level.dimension().equals(Level.END)) {
            return presetBlockStateBitsEndStoneGlobal;
        } else {
            return bottomBlockY >= 0 ? presetBlockStateBitsStoneGlobal : presetBlockStateBitsDeepslateGlobal;
        }
    }

    @Override
    public BlockState[] getPresetBlockStates(Level level, int bottomBlockY) {
        // Return the block states to be added to the paletted containers so that they can be used for obfuscation
        if (bottomBlockY < maxBlockHeight) {
            ResourceKey<Level> resourceKey = level.dimension();
            if (resourceKey.equals(Level.NETHER)) {
                return presetBlockStatesNetherrack;
            } else if (resourceKey.equals(Level.END)) {
                return presetBlockStatesEndStone;
            } else {
                return bottomBlockY >= 0 ? presetBlockStatesStone : presetBlockStatesDeepslate;
            }
        }

        return null;
    }

}
