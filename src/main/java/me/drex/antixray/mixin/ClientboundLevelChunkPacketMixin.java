package me.drex.antixray.mixin;

import io.netty.buffer.ByteBuf;
import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.ClientboundLevelChunkPacketInterface;
import me.drex.antixray.util.LevelChunkSectionInterface;
import me.drex.antixray.util.LevelInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundLevelChunkPacket.class)
public abstract class ClientboundLevelChunkPacketMixin implements ClientboundLevelChunkPacketInterface {

    ChunkPacketInfo<BlockState> chunkPacketInfo;
    @Shadow
    @Final
    private byte[] buffer;

    boolean ready = false;

    @Shadow
    protected abstract ByteBuf getWriteBuffer();


    @Shadow
    public abstract boolean isFullChunk();

    // Replace extractChunkData with our own implementation
    @Redirect(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacket;extractChunkData(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/level/chunk/LevelChunk;I)I"))
    public int replaceExtractChunkData(ClientboundLevelChunkPacket clientboundLevelChunkPacket, FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk, int i) {
        chunkPacketInfo = ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().getChunkPacketInfo((ClientboundLevelChunkPacket) (Object) this, levelChunk);
        if (chunkPacketInfo != null) {
            chunkPacketInfo.setBuffer(this.buffer);
        }
        return this.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk, i, chunkPacketInfo);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;I)V", at = @At(value = "TAIL"))
    public void addFakeBlocks(LevelChunk levelChunk, int i, CallbackInfo ci) {
        ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().modifyBlocks((ClientboundLevelChunkPacket) (Object) this, chunkPacketInfo);
    }

    public int extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk, int i, ChunkPacketInfo<BlockState> chunkPacketInfo) {
        int j = 0;
        LevelChunkSection[] levelChunkSections = levelChunk.getSections();
        int k = 0;

        for (int l = levelChunkSections.length; k < l; ++k) {
            LevelChunkSection levelChunkSection = levelChunkSections[k];
            if (levelChunkSection != LevelChunk.EMPTY_SECTION && (!this.isFullChunk() || !levelChunkSection.isEmpty()) && (i & 1 << k) != 0) {
                j |= 1 << k;
                // Add chunk packet info
                ((LevelChunkSectionInterface) levelChunkSection).write(friendlyByteBuf, chunkPacketInfo);
            }
        }

        return j;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void setReady() {
        ready = true;
    }
}
