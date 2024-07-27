package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.drex.antixray.common.util.Arguments;
import me.drex.antixray.common.util.ChunkPacketInfo;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class ClientboundLevelChunkPacketDataMixin {
    // 1.20.1 - Moved setBuffer to extractChunkData, because mixins can't inject early enough into constructors
    @Inject(method = "extractChunkData", at = @At("HEAD"))
    private static void initializeChunkSectionIndex(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk, CallbackInfo ci, @Share("chunkSectionIndex") LocalIntRef chunkSectionIndexRef) {
        ChunkPacketInfo<BlockState> chunkPacketInfo = Arguments.PACKET_INFO.get();

        if (chunkPacketInfo != null) {
            chunkPacketInfo.setBuffer(friendlyByteBuf.array());
        }
        chunkSectionIndexRef.set(0);
    }

    @WrapOperation(
        method = "extractChunkData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;write(Lnet/minecraft/network/FriendlyByteBuf;)V"
        )
    )
    private static void setChunkSectionIndexArgument(LevelChunkSection instance, FriendlyByteBuf friendlyByteBuf, Operation<Void> original, @Share("chunkSectionIndex") LocalIntRef chunkSectionIndexRef) {
        int chunkSectionIndex = chunkSectionIndexRef.get();

        var previous = Arguments.CHUNK_SECTION_INDEX.get();
        Arguments.CHUNK_SECTION_INDEX.set(chunkSectionIndex);
        try {
            original.call(instance, friendlyByteBuf);
        } finally {
            Arguments.CHUNK_SECTION_INDEX.set(previous);
        }

        chunkSectionIndexRef.set(chunkSectionIndex + 1);
    }
}
