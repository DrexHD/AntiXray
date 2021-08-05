package me.drex.antixray.util;

import me.drex.antixray.config.WorldConfig;

import java.util.concurrent.Executor;

public interface LevelInterface {

    void initValues(Executor executor);

    ChunkPacketBlockController getChunkPacketBlockController();

    WorldConfig getWorldConfig();

}
