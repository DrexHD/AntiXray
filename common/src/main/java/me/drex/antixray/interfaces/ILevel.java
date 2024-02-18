package me.drex.antixray.interfaces;

import me.drex.antixray.util.controller.ChunkPacketBlockController;

public interface ILevel {

    void initValues();

    ChunkPacketBlockController getChunkPacketBlockController();

}
