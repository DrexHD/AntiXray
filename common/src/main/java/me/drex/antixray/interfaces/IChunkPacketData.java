package me.drex.antixray.interfaces;

import me.drex.antixray.util.ChunkPacketInfo;
import net.minecraft.world.level.block.state.BlockState;

public interface IChunkPacketData {
    void customExtractChunkData(ChunkPacketInfo<BlockState> packetInfo);
}
