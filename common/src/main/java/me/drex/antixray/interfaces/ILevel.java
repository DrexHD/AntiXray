package me.drex.antixray.interfaces;

import me.drex.antixray.util.controller.ChunkPacketBlockController;

import java.util.concurrent.Executor;

public interface ILevel {

    void initValues();

    ChunkPacketBlockController getChunkPacketBlockController();

}
