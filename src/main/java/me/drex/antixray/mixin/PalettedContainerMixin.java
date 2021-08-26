package me.drex.antixray.mixin;

import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.PalettedContainerInterface;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements PalettedContainerInterface<T> {

    @Shadow
    protected BitStorage storage;
    private T[] presetValues;
    @Shadow
    private Palette<T> palette;

    @Shadow
    @Final
    private Palette<T> globalPalette;
    @Shadow
    private int bits;
    @Shadow
    @Final
    private IdMapper<T> registry;
    @Shadow
    @Final
    private PaletteResize<T> dummyPaletteResize;
    @Shadow
    @Final
    private Function<CompoundTag, T> reader;
    @Shadow
    @Final
    private Function<T, CompoundTag> writer;

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

    /**
     * @author Drex
     * @reason Add extra blocks to block palette
     */
    @Overwrite
    public void read(ListTag paletteNbt, long[] data) {
        try {
            this.acquire();
            int i = Math.max(4, Mth.ceillog2(paletteNbt.size() + (this.presetValues == null ? 0 : this.presetValues.length))); // Calculate the size with preset values
            if (true || i != this.bits) { // Not initialized yet
                this.setBits(i);
            }

            this.palette.read(paletteNbt);
            this.addPresetValues();
            int j = data.length * 64 / 4096;
            if (this.palette == this.globalPalette) {
                Palette<T> palette = new HashMapPalette<>(this.registry, i, this.dummyPaletteResize, this.reader, this.writer);
                palette.read(paletteNbt);
                BitStorage bitStorage = new BitStorage(i, 4096, data);

                for (int k = 0; k < 4096; ++k) {
                    this.storage.set(k, this.globalPalette.idFor(palette.valueFor(bitStorage.get(k))));
                }
            } else if (j == this.bits) {
                System.arraycopy(data, 0, this.storage.getRaw(), 0, data.length);
            } else {
                BitStorage bitStorage2 = new BitStorage(j, 4096, data);

                for (int l = 0; l < 4096; ++l) {
                    this.storage.set(l, bitStorage2.get(l));
                }
            }
        } finally {
            this.release();
        }

    }

    private void addPresetValues() {
        if (this.presetValues != null && this.palette != this.globalPalette) {
            for (T presetValue : this.presetValues) {
                this.palette.idFor(presetValue);
            }
        }
    }
}
