package me.drex.antixray.mixin;

import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.PalettedContainerInterface;
import me.drex.antixray.util.SimplePalette;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements PalettedContainerInterface<T> {
    private T[] presetValues;

    @Shadow
    private volatile PalettedContainer.Data<T> data;

    @Shadow
    public abstract void acquire();

    @Shadow
    public abstract void release();


    @Override
    public void initValues(T[] presetValues) {
        this.presetValues = presetValues;
        this.addPresetValues();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY) {
        this.acquire();
        try {
            this.write(friendlyByteBuf, chunkPacketInfo, this.presetValues, bottomBlockY);
        } finally {
            this.release();
        }
    }

    private void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, T[] presetValues, int bottomBlockY) {
        this.addPresetValues();
        friendlyByteBuf.writeByte(this.data.storage().getBits());
        this.data.palette().write(friendlyByteBuf);
        // Add chunk packet info
        if (chunkPacketInfo != null) {
            // Bottom block to 0 based chunk section index
            int chunkSectionIndex = (bottomBlockY >> 4) - chunkPacketInfo.getChunk().getMinSection();
            chunkPacketInfo.setBits(chunkSectionIndex, this.data.storage().getBits());
            chunkPacketInfo.setPalette(chunkSectionIndex, this.getPaletteCopy(this.data.palette()));
            chunkPacketInfo.setIndex(chunkSectionIndex, friendlyByteBuf.writerIndex() + FriendlyByteBuf.getVarIntSize(this.data.storage().getRaw().length));
            chunkPacketInfo.setPresetValues(chunkSectionIndex, presetValues);
        }
        friendlyByteBuf.writeLongArray(this.data.storage().getRaw());
    }

    /**
    * Because obfuscation is done async, the palette might change size. This is problematic, because the size is already
    * written to the {@link FriendlyByteBuf}. The solution is to make a copy of the palette at the point of writing
     * and forwarding that to the ChunkPacketInfo.
    * */
    private Palette<T> getPaletteCopy(Palette<T> original) {
        if (original instanceof GlobalPalette) {
            // GlobalPalette won't change, so we don't need to copy
            return original;
        } else {
            T[] values = (T[]) new Object[original.getSize()];
            for (int i = 0; i < original.getSize(); i++) {
                values[i] = original.valueFor(i);
            }
            return new SimplePalette<>(values);
        }
    }

    private void addPresetValues() {
        if (this.presetValues != null) {
            for (T presetValue : this.presetValues) {
                this.data.palette().idFor(presetValue);
            }
        }
    }
}
