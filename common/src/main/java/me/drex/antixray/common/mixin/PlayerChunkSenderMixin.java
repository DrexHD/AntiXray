package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.drex.antixray.common.AntiXray;
import me.drex.antixray.common.interfaces.IClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {

    @WrapOperation(
        method = "sendNextChunks",
        at = @At(
            value = "NEW",
            target = "()Lnet/minecraft/network/protocol/game/ClientboundChunkBatchStartPacket;"
        )
    )
    private ClientboundChunkBatchStartPacket addBatchSizeArgument(Operation<ClientboundChunkBatchStartPacket> original, @Local List<LevelChunk> list, @Share("startPacket") LocalRef<IClientboundChunkBatchStartPacket> ipacketRef) {
        ClientboundChunkBatchStartPacket startPacket = original.call();
        IClientboundChunkBatchStartPacket iPacket = (IClientboundChunkBatchStartPacket) startPacket;
        iPacket.antixray$setBatchSize(list.size());
        ipacketRef.set(iPacket);
        return startPacket;
    }

    @WrapOperation(
        method = "sendNextChunks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/PlayerChunkSender;sendChunk(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;)V"
        )
    )
    private void clearBatchPacket(ServerGamePacketListenerImpl serverGamePacketListenerImpl, ServerLevel serverLevel, LevelChunk levelChunk, Operation<Void> original, @Share("startPacket") LocalRef<IClientboundChunkBatchStartPacket> ipacketRef) {
        // Pass the batch start packet to the chunk packets
        AntiXray.BATCH_START_PACKET.set(ipacketRef.get());
        try {
            original.call(serverGamePacketListenerImpl, serverLevel, levelChunk);
        } finally {
            AntiXray.BATCH_START_PACKET.remove();
        }
    }

}
