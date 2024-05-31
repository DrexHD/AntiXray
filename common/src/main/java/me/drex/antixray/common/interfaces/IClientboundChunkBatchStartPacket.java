package me.drex.antixray.common.interfaces;

public interface IClientboundChunkBatchStartPacket extends IPacket {

    void antixray$setBatchSize(int batchSize);

    void antixray$notifyChunkReady();

}
