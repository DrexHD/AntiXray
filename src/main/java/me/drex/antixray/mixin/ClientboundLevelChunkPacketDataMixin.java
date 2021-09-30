package me.drex.antixray.mixin;

import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.ClientboundLevelChunkPacketDataInterface;
import me.drex.antixray.util.LevelChunkSectionInterface;
import me.drex.antixray.util.LevelInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
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

@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class ClientboundLevelChunkPacketDataMixin implements ClientboundLevelChunkPacketDataInterface {

    ChunkPacketInfo<BlockState> chunkPacketInfo;
    private volatile boolean ready = false;

    @Shadow
    @Final
    private byte[] buffer;

    private static FriendlyByteBuf cachedFriendlyByteBuf;
    private static LevelChunk cachedLevelChunk;

    // Cancel vanilla extractChunkData and store variables for our own implementation
    @Redirect(
            method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;extractChunkData(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/level/chunk/LevelChunk;)V"
            )
    )
    private void replaceExtractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        cachedFriendlyByteBuf = friendlyByteBuf;
        cachedLevelChunk = levelChunk;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V",
            at = @At("TAIL")
    )
    public void addFakeBlocks(LevelChunk levelChunk, CallbackInfo ci) {
        this.customExtractChunkData(cachedFriendlyByteBuf, cachedLevelChunk);
        ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().modifyBlocks((ClientboundLevelChunkPacketData) (Object) this, chunkPacketInfo);
    }

    public void customExtractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        this.chunkPacketInfo = ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().getChunkPacketInfo((ClientboundLevelChunkPacketData) (Object) this, levelChunk);
        if (chunkPacketInfo != null) {
            chunkPacketInfo.setBuffer(this.buffer);
        }
        LevelChunkSection[] levelChunkSections = levelChunk.getSections();

        for (LevelChunkSection levelChunkSection : levelChunkSections) {
            ((LevelChunkSectionInterface) levelChunkSection).write(friendlyByteBuf, this.chunkPacketInfo);
        }
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
