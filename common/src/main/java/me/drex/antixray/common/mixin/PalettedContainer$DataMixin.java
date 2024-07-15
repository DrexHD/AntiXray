package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.antixray.common.util.Arguments;
import me.drex.antixray.common.util.ChunkPacketInfo;
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

@Mixin(PalettedContainer.Data.class)
public abstract class PalettedContainer$DataMixin<T> {
    @Shadow
    @Final
    Palette<T> palette;

    /**
     * Polymer has a <a href="https://github.com/Patbox/polymer/blob/dev/1.21/polymer-core/src/main/java/eu/pb4/polymer/core/mixin/block/packet/PalettedContainerDataMixin.java#L26-L57">mixin</a>
     * to change the bit count of the palette when writing. We need to make sure we get the updated bit count value.
     */
    @WrapOperation(
        method = "write",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/BitStorage;getRaw()[J"
        )
    )
    public long[] initializeChunkPacketInfo(BitStorage storage, Operation<long[]> original, FriendlyByteBuf buf) {
        // custom arguments
        ChunkPacketInfo<BlockState> chunkPacketInfo = Arguments.PACKET_INFO.get();
        Integer chunkSectionIndex = Arguments.CHUNK_SECTION_INDEX.get();

        if (chunkPacketInfo != null) {
            chunkPacketInfo.setBits(chunkSectionIndex, storage.getBits());
            //noinspection unchecked
            chunkPacketInfo.setPalette(chunkSectionIndex, (Palette<BlockState>) this.palette);
            chunkPacketInfo.setIndex(chunkSectionIndex, buf.writerIndex() + VarInt.getByteSize(storage.getRaw().length));
        }
        return original.call(storage);
    }
}
