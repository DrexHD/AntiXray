package me.drex.antixray.common.mixin;

import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientboundChunkBatchStartPacket.class)
public interface ClientboundChunkBatchStartPacketAccessor {
    @Invoker("<init>")
    static ClientboundChunkBatchStartPacket init() {
        throw new AssertionError();
    }
}
