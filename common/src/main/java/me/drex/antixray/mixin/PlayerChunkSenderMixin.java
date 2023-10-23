package me.drex.antixray.mixin;

import me.drex.antixray.interfaces.IChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.BitSet;

@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {

    @Redirect(
            method = "sendChunk",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)Lnet/minecraft/network/protocol/game/ClientboundLevelChunkWithLightPacket;"
            )
    )
    private static ClientboundLevelChunkWithLightPacket modifyChunkPacket(LevelChunk chunk, LevelLightEngine lightEngine, BitSet bitSet, BitSet bitSet2, ServerGamePacketListenerImpl listener, ServerLevel level, LevelChunk levelChunk) {
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, lightEngine, bitSet, bitSet2);
        ((IChunkPacket) packet).modifyPacket(levelChunk, listener.player);
        return packet;
    }
}
