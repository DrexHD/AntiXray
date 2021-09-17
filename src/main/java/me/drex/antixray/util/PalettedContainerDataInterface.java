package me.drex.antixray.util;

import net.minecraft.network.FriendlyByteBuf;

public interface PalettedContainerDataInterface<T> {

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, T[] presetValues, int bottomBlockY);

}
