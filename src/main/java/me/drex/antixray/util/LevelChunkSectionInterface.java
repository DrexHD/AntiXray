package me.drex.antixray.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelChunkSectionInterface {

    void addBlockPresets(Level level);

    void initValues(LevelHeightAccessor chunkAccess);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<BlockState> chunkPacketInfo);

}
