package me.drex.antixray.interfaces;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

public interface IChunkPacket {

    boolean isReady();

    void setReady(boolean ready);

    void modifyPacket(LevelChunk chunk, ServerPlayer player);
}
