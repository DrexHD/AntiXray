package me.drex.antixray.common.mixin;

import me.drex.antixray.common.util.Arguments;
import me.drex.antixray.common.util.ChunkPacketInfo;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PalettedContainer.Data.class)
public abstract class PalettedContainer$DataMixin<T> {

    @Shadow
    @Final
    private PalettedContainer.Configuration<T> configuration;

    @Shadow
    @Final
    Palette<T> palette;

    @Shadow
    @Final
    BitStorage storage;

    @Inject(
        method = "write",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/FriendlyByteBuf;writeLongArray([J)Lnet/minecraft/network/FriendlyByteBuf;"
        )
    )
    public void initializeChunkPacketInfo(FriendlyByteBuf buf, CallbackInfo ci) {
        // custom arguments
        ChunkPacketInfo<BlockState> chunkPacketInfo = Arguments.PACKET_INFO.get();
        Integer chunkSectionIndex = Arguments.CHUNK_SECTION_INDEX.get();

        if (chunkPacketInfo != null) {
            chunkPacketInfo.setBits(chunkSectionIndex, this.configuration.bits());
            //noinspection unchecked
            chunkPacketInfo.setPalette(chunkSectionIndex, (Palette<BlockState>) this.palette);
            chunkPacketInfo.setIndex(chunkSectionIndex, buf.writerIndex() + VarInt.getByteSize(storage.getRaw().length));
        }
    }

}
