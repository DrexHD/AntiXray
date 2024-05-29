package me.drex.antixray.common.util;

import me.drex.antixray.common.util.controller.ChunkPacketBlockControllerAntiXray;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ChunkPacketInfoAntiXray extends ChunkPacketInfo<BlockState> implements Runnable {

    private final ChunkPacketBlockControllerAntiXray chunkPacketBlockControllerAntiXray;
    private ChunkAccess[] nearbyChunks;

    public ChunkPacketInfoAntiXray(ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk, ChunkPacketBlockControllerAntiXray chunkPacketBlockControllerAntiXray) {
        super(chunkPacket, chunk);
        this.chunkPacketBlockControllerAntiXray = chunkPacketBlockControllerAntiXray;
    }

    public ChunkAccess[] getNearbyChunks() {
        return nearbyChunks;
    }

    public void setNearbyChunks(ChunkAccess... nearbyChunks) {
        this.nearbyChunks = nearbyChunks;
    }

    @Override
    public void run() {
        chunkPacketBlockControllerAntiXray.obfuscate(this);
    }
}