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
            chunkPacketInfo.setPalette(chunkSectionIndex, this.data.palette());
            chunkPacketInfo.setIndex(chunkSectionIndex, friendlyByteBuf.writerIndex() + FriendlyByteBuf.getVarIntSize(this.data.storage().getRaw().length));
            chunkPacketInfo.setPresetValues(chunkSectionIndex, presetValues);
        }
        friendlyByteBuf.writeLongArray(this.data.storage().getRaw());
    }

    private void addPresetValues() {
        if (this.presetValues != null) {
            for (T presetValue : this.presetValues) {
                this.data.palette().idFor(presetValue);
            }
        }
    }
}
