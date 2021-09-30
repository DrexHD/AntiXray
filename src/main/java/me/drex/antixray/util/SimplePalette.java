package me.drex.antixray.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.Palette;

import java.util.function.Predicate;

public record SimplePalette<T>(T[] values) implements Palette<T> {

    @Override
    public int idFor(T object) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(object)) return i;
        }
        return -1;
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T valueFor(int i) {
        return values[i];
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSerializedSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        return values.length;
    }
}
