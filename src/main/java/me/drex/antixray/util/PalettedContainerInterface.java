package me.drex.antixray.util;

import net.minecraft.network.FriendlyByteBuf;

public interface PalettedContainerInterface<T> {

    void initValues(T[] presetValues);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY);

}
