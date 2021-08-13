package me.drex.antixray.util;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.Palette;

public class ChunkPacketInfo<T> {

    private final ClientboundLevelChunkPacket chunkPacket;
    private final LevelChunk chunk;
    private final int[] bits;
    private final Object[] palettes;
    private final int[] indexes;
    private final Object[][] presetValues;
    private byte[] buffer;

    public ChunkPacketInfo(ClientboundLevelChunkPacket chunkPacket, LevelChunk chunk) {
        this.chunkPacket = chunkPacket;
        this.chunk = chunk;
        final int sections = 16;
        bits = new int[sections];
        palettes = new Object[sections];
        indexes = new int[sections];
        presetValues = new Object[sections][];
    }

    public ClientboundLevelChunkPacket getChunkPacket() {
        return chunkPacket;
    }

    public LevelChunk getChunk() {
        return chunk;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getBits(int chunkSectionIndex) {
        return bits[chunkSectionIndex];
    }

    public void setBits(int chunkSectionIndex, int bits) {
        this.bits[chunkSectionIndex] = bits;
    }

    @SuppressWarnings("unchecked")
    public Palette<T> getPalette(int chunkSectionIndex) {
        return (Palette<T>) palettes[chunkSectionIndex];
    }

    public void setPalette(int chunkSectionIndex, Palette<T> palette) {
        palettes[chunkSectionIndex] = palette;
    }

    public int getIndex(int chunkSectionIndex) {
        return indexes[chunkSectionIndex];
    }

    public void setIndex(int chunkSectionIndex, int index) {
        indexes[chunkSectionIndex] = index;
    }

    @SuppressWarnings("unchecked")
    public T[] getPresetValues(int chunkSectionIndex) {
        return (T[]) presetValues[chunkSectionIndex];
    }

    public void setPresetValues(int chunkSectionIndex, T[] presetValues) {
        this.presetValues[chunkSectionIndex] = presetValues;
    }

    public boolean isWritten(int chunkSectionIndex) {
        return bits[chunkSectionIndex] != 0;
    }
}
