package me.drex.antixray.interfaces;

import me.drex.antixray.util.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IChunkSection {

    void addBlockPresets(Level level);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<BlockState> chunkPacketInfo);
}
