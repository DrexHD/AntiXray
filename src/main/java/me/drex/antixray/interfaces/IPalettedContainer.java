package me.drex.antixray.interfaces;

import me.drex.antixray.util.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;

public interface IPalettedContainer<T> {

    // Only used in ChunkSerializer when the palette gets read from NBT.
    void addPresetValuesWithEntries(T[] presetValues);

    // Used on all other constructors for PalettedContainer.
    void addPresetValues(T[] presetValues);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY);
}