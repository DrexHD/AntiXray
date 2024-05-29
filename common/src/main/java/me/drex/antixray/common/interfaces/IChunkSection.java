package me.drex.antixray.common.interfaces;

import me.drex.antixray.common.util.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IChunkSection {

    void init(int bottomBlockY);

    void addBlockPresets(Level level);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<BlockState> chunkPacketInfo);
}
