package me.drex.antixray.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class ChunkManager {

    /**
     * Returns the BlockState at {@param pos} in {@param level} if the position is loaded.
     */

    @Nullable
    public static BlockState getStateIfLoaded(Level level, BlockPos pos) {
        final LevelChunk chunk = getChunkIfLoaded(level, pos);
        return chunk != null ? chunk.getBlockState(pos) : Blocks.VOID_AIR.defaultBlockState();
    }

    /**
     * Returns the chunk at {@param pos} in {@param level} if the position is loaded.
     */

    @Nullable
    public static LevelChunk getChunkIfLoaded(Level level, BlockPos pos) {
        return getChunkIfLoaded(level, pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    public static LevelChunk getChunkIfLoaded(Level level, int chunkX, int chunkZ) {
        final ChunkHolder holder = getChunkHolder(level, chunkX, chunkZ);
        return holder != null ? holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().orElse(null) : null;
    }

    /**
     * Returns the ChunkHolder at chunk coordinate X / Z in {@param level} if the location is loaded.
     */

    @Nullable
    public static ChunkHolder getChunkHolder(Level level, int chunkX, int chunkZ) {
        return level.getChunkSource() instanceof ServerChunkCache chunkCache ? chunkCache.getVisibleChunkIfPresent(ChunkPos.asLong(chunkX, chunkZ)) : null;
    }
}