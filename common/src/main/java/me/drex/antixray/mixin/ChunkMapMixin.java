package me.drex.antixray.mixin;

import me.drex.antixray.interfaces.IChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Inject(
            method = "playerLoadedChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;trackChunk(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/network/protocol/Packet;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void modifyChunkPacket(ServerPlayer serverPlayer, MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject, LevelChunk levelChunk, CallbackInfo ci) {
        ClientboundLevelChunkWithLightPacket chunkWithLightPacket = mutableObject.getValue();
        ((IChunkPacket) chunkWithLightPacket).modifyPacket(serverPlayer);
    }

}
