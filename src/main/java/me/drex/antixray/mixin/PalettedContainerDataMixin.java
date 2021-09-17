package me.drex.antixray.mixin;

import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.PalettedContainerDataInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.Data.class)
public class PalettedContainerDataMixin<T> implements PalettedContainerDataInterface<T> {

    @Shadow
    @Final
    private BitStorage storage;

    @Shadow
    @Final
    private Palette<T> palette;

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, T[] presetValues, int bottomBlockY) {
        friendlyByteBuf.writeByte(this.storage.getBits());
        this.palette.write(friendlyByteBuf);
        // Add chunk packet info
        if (chunkPacketInfo != null) {
            // Bottom block to 0 based chunk section index
            int chunkSectionIndex = (bottomBlockY >> 4) - chunkPacketInfo.getChunk().getMinSection();
            chunkPacketInfo.setBits(chunkSectionIndex, this.storage.getBits());
            chunkPacketInfo.setPalette(chunkSectionIndex, this.palette);
            chunkPacketInfo.setIndex(chunkSectionIndex, friendlyByteBuf.writerIndex() + FriendlyByteBuf.getVarIntSize(this.storage.getRaw().length));
            chunkPacketInfo.setPresetValues(chunkSectionIndex, presetValues);
        }
        friendlyByteBuf.writeLongArray(this.storage.getRaw());
    }
}