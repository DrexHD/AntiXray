package me.drex.antixray.common.interfaces;

import me.drex.antixray.common.util.ChunkPacketInfo;
import net.minecraft.world.level.block.state.BlockState;

public interface IChunkPacketData {
    void customExtractChunkData(ChunkPacketInfo<BlockState> packetInfo);
}
