package me.drex.antixray.util;

import net.minecraft.world.level.chunk.LevelChunk;

public interface ServerLevelInterface {

    LevelChunk getChunkIfLoaded(int x, int z);

}
