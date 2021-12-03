package me.drex.antixray.interfaces;

import me.drex.antixray.util.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;

public interface IPalettedContainer<T> {

    void addPresetValuesWithEntries(T[] presetValues);

    void addPresetValues(T[] presetValues);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY);
}