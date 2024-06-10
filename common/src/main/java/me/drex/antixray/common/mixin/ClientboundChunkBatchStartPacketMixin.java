package me.drex.antixray.common.mixin;

import me.drex.antixray.common.interfaces.IClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientboundChunkBatchStartPacket.class)
public abstract class ClientboundChunkBatchStartPacketMixin implements IClientboundChunkBatchStartPacket {

    @Unique
    private int antixray$batchSize;

    @Unique
    private int antixray$readyPackets = 0;

    @Override
    public void antixray$setBatchSize(int batchSize) {
        this.antixray$batchSize = batchSize;
    }

    @Override
    public synchronized void antixray$notifyChunkReady() {
        antixray$readyPackets++;
    }

    /**
     * We need to delay the chunk batch start packet until all of its packets are ready.
     * This is to ensure that all packets between batch start and end can be sent to the client immediately, allowing 
     * the client to correctly calculate its desired batch size.
     */
    @Override
    public boolean isAntixray$ready() {
        return antixray$readyPackets >= antixray$batchSize;
    }
}
