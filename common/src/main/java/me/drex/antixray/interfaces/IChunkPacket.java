package me.drex.antixray.interfaces;

import net.minecraft.server.level.ServerPlayer;

public interface IChunkPacket {

    void modifyPacket(ServerPlayer player);

    boolean isReady();

    void setReady(boolean ready);
}
