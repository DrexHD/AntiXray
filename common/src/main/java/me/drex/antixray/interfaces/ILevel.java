package me.drex.antixray.interfaces;

import me.drex.antixray.config.WorldConfig;
import me.drex.antixray.util.ChunkPacketBlockController;

import java.util.concurrent.Executor;

public interface ILevel {

    void initValues(Executor executor);

    ChunkPacketBlockController getChunkPacketBlockController();

    WorldConfig getWorldConfig();
}
