package me.drex.antixray.interfaces;

public interface IChunkPacket {

    void modifyPacket(boolean shouldModify);

    boolean isReady();

    void setReady(boolean ready);
}
