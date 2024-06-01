package me.drex.antixray.common.mixin;

import me.drex.antixray.common.AntiXray;
import me.drex.antixray.common.interfaces.IChunkPacket;
import me.drex.antixray.common.interfaces.IChunkPacketData;
import me.drex.antixray.common.interfaces.IClientboundChunkBatchStartPacket;
import me.drex.antixray.common.util.ChunkPacketInfo;
import me.drex.antixray.common.util.Util;
import me.drex.antixray.common.util.controller.ChunkPacketBlockController;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public abstract class ClientboundLevelChunkWithLightPacketMixin implements IChunkPacket {

    @Shadow
    @Final
    private ClientboundLevelChunkPacketData chunkData;

    @Unique
    boolean antixray$ready = false;

    @Unique
    IClientboundChunkBatchStartPacket antixray$batchStartPacket;

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V",
        at = @At("TAIL")
    )
    private void onInit(LevelChunk chunk, LevelLightEngine levelLightEngine, BitSet bitSet, BitSet bitSet2, CallbackInfo ci) {
        this.antixray$batchStartPacket = AntiXray.BATCH_START_PACKET.get();
        final ClientboundLevelChunkWithLightPacket packet = (ClientboundLevelChunkWithLightPacket) (Object) this;
        final ChunkPacketBlockController controller = Util.getBlockController(chunk.getLevel());
        final ChunkPacketInfo<BlockState> packetInfo = controller.getChunkPacketInfo(packet, chunk);
        ((IChunkPacketData) this.chunkData).customExtractChunkData(packetInfo);
        controller.modifyBlocks(packet, packetInfo);
    }

    @Override
    public boolean isAntixray$ready() {
        return antixray$ready;
    }

    @Override
    public void antixray$setReady(boolean antixray$ready) {
        this.antixray$ready = antixray$ready;
        if (antixray$batchStartPacket != null) {
            // Chunk packets may not have a batch start packet, if they are manually sent by other mods
            antixray$batchStartPacket.antixray$notifyChunkReady();
        }
    }


}
