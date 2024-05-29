package me.drex.antixray.common.interfaces;

import me.drex.antixray.common.util.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;

public interface IPalettedContainer<T> {

    void addPresetValuesWithEntries(T[] presetValues);

    void addPresetValues(T[] presetValues);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY);
}