package me.drex.antixray.mixin;

import me.drex.antixray.interfaces.IChunkPacket;
import me.drex.antixray.interfaces.IChunkPacketData;
import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.Util;
import me.drex.antixray.util.controller.ChunkPacketBlockController;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public abstract class ClientboundLevelChunkWithLightPacketMixin implements IChunkPacket {
    @Unique
    private volatile boolean ready = false;

    @Shadow
    @Final
    private ClientboundLevelChunkPacketData chunkData;

    @Override
    public void modifyPacket(LevelChunk chunk, ServerPlayer player) {
        final ClientboundLevelChunkWithLightPacket packet = (ClientboundLevelChunkWithLightPacket) (Object) this;
        final ChunkPacketBlockController controller = Util.getBlockController(player);
        final ChunkPacketInfo<BlockState> packetInfo = controller.getChunkPacketInfo(packet, chunk);

        ((IChunkPacketData) this.chunkData).customExtractChunkData(packetInfo);
        controller.modifyBlocks(packet, packetInfo);
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
