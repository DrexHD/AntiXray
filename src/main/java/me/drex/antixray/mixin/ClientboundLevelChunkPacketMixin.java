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

import java.util.BitSet;

@Mixin(ClientboundLevelChunkPacket.class)
public abstract class ClientboundLevelChunkPacketMixin implements ClientboundLevelChunkPacketInterface {

    ChunkPacketInfo<BlockState> chunkPacketInfo;
    @Shadow
    @Final
    private byte[] buffer;

    boolean ready = false;

    @Shadow
    protected abstract ByteBuf getWriteBuffer();

    @Redirect(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacket;extractChunkData(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/level/chunk/LevelChunk;)Ljava/util/BitSet;"))
    public BitSet assignAvailableSections(ClientboundLevelChunkPacket clientboundLevelChunkPacket, FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        chunkPacketInfo = ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().getChunkPacketInfo((ClientboundLevelChunkPacket) (Object) this, levelChunk);
        if (chunkPacketInfo != null) {
            chunkPacketInfo.setBuffer(this.buffer);
        }
        return this.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk, chunkPacketInfo);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At(value = "TAIL"))
    public void onInitEnd(LevelChunk levelChunk, CallbackInfo ci) {
        ((LevelInterface) levelChunk.getLevel()).getChunkPacketBlockController().modifyBlocks((ClientboundLevelChunkPacket) (Object) this, chunkPacketInfo);
    }

    public BitSet extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk, ChunkPacketInfo<BlockState> chunkPacketInfo) {
        BitSet bitSet = new BitSet();
        LevelChunkSection[] levelChunkSections = levelChunk.getSections();
        int i = 0;

        for (int j = levelChunkSections.length; i < j; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSections[i];
            if (levelChunkSection != LevelChunk.EMPTY_SECTION && !levelChunkSection.isEmpty()) {
                bitSet.set(i);
                ((LevelChunkSectionInterface)levelChunkSection).write(friendlyByteBuf, chunkPacketInfo); // Paper - Anti-Xray - Add chunk packet info
            }
        }

        return bitSet;
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
