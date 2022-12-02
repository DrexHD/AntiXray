package me.drex.antixray.mixin;

import me.drex.antixray.AntiXray;
import me.drex.antixray.interfaces.IChunkPacket;
import me.drex.antixray.interfaces.IChunkPacketData;
import me.drex.antixray.util.ChunkPacketBlockController;
import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.Util;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public abstract class ClientboundLevelChunkWithLightPacketMixin implements IChunkPacket {
    @Unique
    private volatile boolean ready = false;

    @Shadow
    @Final
    private ClientboundLevelChunkPacketData chunkData;

    @Unique
    private LevelChunk chunk;

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;Z)V",
            at = @At("TAIL")
    )
    private void captureChunk(LevelChunk chunk, LevelLightEngine levelLightEngine, BitSet bitSet, BitSet bitSet2, boolean bl, CallbackInfo ci) {
        this.chunk = chunk;
    }

    @Override
    public void modifyPacket(ServerPlayer player) {
        final ClientboundLevelChunkWithLightPacket packet = (ClientboundLevelChunkWithLightPacket) (Object) this;
        boolean bypassXray = AntiXray.INSTANCE.canBypassXray(player);
        final ChunkPacketBlockController controller = bypassXray ? ChunkPacketBlockController.NO_OPERATION_INSTANCE : Util.getBlockController(chunk.getLevel());
        final ChunkPacketInfo<BlockState> packetInfo = controller.getChunkPacketInfo(packet, chunk);
        ((IChunkPacketData) this.chunkData).customExtractChunkData(packetInfo);
        controller.modifyBlocks(packet, packetInfo);
        chunk = null;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}