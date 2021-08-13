package me.drex.antixray.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface LevelChunkSectionInterface {

    void initValues(Level level, boolean initializeBlocks);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<BlockState> chunkPacketInfo);

}
