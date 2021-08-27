package me.drex.antixray.mixin;

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

import java.util.BitSet;

@Mixin(ClientboundLevelChunkPacket.class)
public abstract class ClientboundLevelChunkPacketMixin implements ClientboundLevelChunkPacketInterface {

    ChunkPacketInfo<BlockState> chunkPacketInfo;
    private volatile boolean ready = false;

    @Shadow
    @Final
    private byte[] buffer;

    @Shadow
    public abstract BitSet extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk);

    // Add chunk packet info
    @Redirect(
            method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacket;extractChunkData(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/level/chunk/LevelChunk;)Ljava/util/BitSet;"
            )
    )
    public BitSet replaceExtractChunkData(ClientboundLevelChunkPacket clientboundLevelChunkPacket, FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        this.chunkPacketInfo = ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().getChunkPacketInfo((ClientboundLevelChunkPacket) (Object) this, levelChunk);
        if (chunkPacketInfo != null) {
            chunkPacketInfo.setBuffer(this.buffer);
        }
        return this.extractChunkData(friendlyByteBuf, levelChunk);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V",
            at = @At("TAIL")
    )
    public void addFakeBlocks(LevelChunk levelChunk, CallbackInfo ci) {
        ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().modifyBlocks((ClientboundLevelChunkPacket) (Object) this, chunkPacketInfo);
    }

    // Replace chunk section writing with our own implementation
    @Redirect(
            method = "extractChunkData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;write(Lnet/minecraft/network/FriendlyByteBuf;)V"
            )
    )
    private void extractChunkData(LevelChunkSection levelChunkSection, FriendlyByteBuf friendlyByteBuf) {
        ((LevelChunkSectionInterface) levelChunkSection).write(friendlyByteBuf, this.chunkPacketInfo);
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
