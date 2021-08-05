package me.drex.antixray.util;

import net.minecraft.network.FriendlyByteBuf;

public interface PalettedContainerInterface<T> {

    void initValues(T[] presetValues, boolean initialize);

    void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY);

}
