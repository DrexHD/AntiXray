package me.drex.antixray.common.interfaces;

import me.drex.antixray.common.util.controller.ChunkPacketBlockController;

public interface ILevel {

    void initValues();

    ChunkPacketBlockController getChunkPacketBlockController();

}
