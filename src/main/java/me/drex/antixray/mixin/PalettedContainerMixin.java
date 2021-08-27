package me.drex.antixray.mixin;

import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.PalettedContainerInterface;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements PalettedContainerInterface<T> {
    private T[] presetValues;

    @Shadow
    protected BitStorage storage;

    @Shadow
    private Palette<T> palette;

    @Shadow
    @Final
    private Palette<T> globalPalette;

    @Shadow
    private int bits;

    @Shadow
    protected abstract void setBits(int i);

    @Shadow
    public abstract void acquire();

    @Shadow
    public abstract void release();

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/PalettedContainer;setBits(I)V"
            )
    )
    public void onSetBits(PalettedContainer<T> palettedContainer, int i) {
        // We do our own calculations later
    }

    @Override
    public void initValues(T[] presetValues, boolean initialize) {
        this.presetValues = presetValues;
        if (initialize) {
            if (presetValues == null) {
                // Default
                this.setBits(4);
            } else {
                // Count the number of required bits
                // Preset values:   presetValues.length - 1
                // Air:                                 + 1
                // Extra:                              + 15
                // Air and extra correspond to the default behavior this.setBits(4)
                this.setBits(32 - Integer.numberOfLeadingZeros(presetValues.length + 15));
                this.addPresetValues();
            }
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY) {
        try {
            this.acquire();
            friendlyByteBuf.writeByte(this.bits);
            this.palette.write(friendlyByteBuf);
            // Add chunk packet info
            if (chunkPacketInfo != null) {
                // Bottom block to 0 based chunk section index
                int chunkSectionIndex = (bottomBlockY >> 4) - chunkPacketInfo.getChunk().getMinSection();
                chunkPacketInfo.setBits(chunkSectionIndex, this.bits);
                chunkPacketInfo.setPalette(chunkSectionIndex, this.palette);
                chunkPacketInfo.setIndex(chunkSectionIndex, friendlyByteBuf.writerIndex() + FriendlyByteBuf.getVarIntSize(this.storage.getRaw().length));
                chunkPacketInfo.setPresetValues(chunkSectionIndex, this.presetValues);
            }
            friendlyByteBuf.writeLongArray(this.storage.getRaw());
        } finally {
            this.release();
        }
    }

    @Inject(
            method = "onResize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/PalettedContainer;setBits(I)V",
                    shift = At.Shift.AFTER
            )
    )
    public void addPresetValues(int i, T object, CallbackInfoReturnable<Integer> cir) {
        this.addPresetValues();
    }

    @Redirect(
            method = "read(Lnet/minecraft/nbt/ListTag;[J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;max(II)I",
                    ordinal = 0
            )
    )
    private int modifyBits(int a, int b, ListTag paletteNbt, long[] data) {
        return Math.max(a, Mth.ceillog2(paletteNbt.size() + (this.presetValues == null ? 0 : this.presetValues.length)));
    }

    @Redirect(
            method = "read(Lnet/minecraft/nbt/ListTag;[J)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/chunk/PalettedContainer;bits:I",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private int redirectBits(PalettedContainer<T> palettedContainer) {
        // i != this.bits should always return true here
        return -1;
    }

    @Inject(
            method = "read(Lnet/minecraft/nbt/ListTag;[J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/Palette;read(Lnet/minecraft/nbt/ListTag;)V",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            )
    )
    private void addPresetValues(ListTag listTag, long[] ls, CallbackInfo ci) {
        this.addPresetValues();
    }

    private void addPresetValues() {
        if (this.presetValues != null && this.palette != this.globalPalette) {
            for (T presetValue : this.presetValues) {
                this.palette.idFor(presetValue);
            }
        }
    }
}
