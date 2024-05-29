package me.drex.antixray.common.util;

import me.drex.antixray.common.interfaces.ILevel;
import me.drex.antixray.common.interfaces.IPacket;
import me.drex.antixray.common.util.controller.ChunkPacketBlockController;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

public final class Util {

    public static ChunkPacketBlockController getBlockController(Level level) {
        return ((ILevel) level).getChunkPacketBlockController();
    }

    // Converts height accessors from ChunkAccess into levels.
    public static Level getLevel(LevelHeightAccessor heightAccessor) {
        if (heightAccessor instanceof Level level) {
            return level;
        } else if (heightAccessor instanceof LevelChunk levelChunk) {
            return levelChunk.getLevel();
        } else if (heightAccessor instanceof ChunkAccess chunk) {
            return getLevel(chunk.levelHeightAccessor);
        } else {
            // If we return null, chunks will not get properly obfuscated.
            throw new IllegalStateException("Failed to add block presets as height accessor was an instance of " + heightAccessor.getClass().getSimpleName());
        }
    }

    public static boolean isReady(Packet<?> packet) {
        if (packet instanceof IPacket iPacket) {
            return iPacket.isReady();
        }
        return true;
    }
}
