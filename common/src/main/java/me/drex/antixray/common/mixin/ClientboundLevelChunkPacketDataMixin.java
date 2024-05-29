package me.drex.antixray.common.mixin;

import me.drex.antixray.common.interfaces.IChunkPacketData;
import me.drex.antixray.common.interfaces.IChunkSection;
import me.drex.antixray.common.util.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class ClientboundLevelChunkPacketDataMixin implements IChunkPacketData {
    @Shadow
    @Final
    private byte[] buffer;

    @Unique
    private FriendlyByteBuf byteBuf;

    @Unique
    private LevelChunk chunk;

    @Redirect(
        method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;extractChunkData(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/level/chunk/LevelChunk;)V"
        )
    )
    private void prepareVariables(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        this.byteBuf = friendlyByteBuf;
        this.chunk = levelChunk;
    }

    @Override
    public void customExtractChunkData(ChunkPacketInfo<BlockState> packetInfo) {
        if (packetInfo != null) {
            packetInfo.setBuffer(this.buffer);
        }

        for (LevelChunkSection section : this.chunk.getSections()) {
            ((IChunkSection) section).write(this.byteBuf, packetInfo);
        }
    }
}
