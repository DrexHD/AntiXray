package me.drex.antixray.util;

import me.drex.antixray.interfaces.IChunkPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkPacketBlockController {

    public static final ChunkPacketBlockController NO_OPERATION_INSTANCE = new ChunkPacketBlockController();

    protected ChunkPacketBlockController() {
    }

    public BlockState[] getPresetBlockStates(Level level, int bottomBlockY) {
        return null;
    }

    public ChunkPacketInfo<BlockState> getChunkPacketInfo(ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk) {
        return null;
    }

    public void modifyBlocks(ClientboundLevelChunkWithLightPacket chunkPacket, ChunkPacketInfo<BlockState> chunkPacketInfo) {
        ((IChunkPacket) chunkPacket).setReady(true);
    }

    public void onBlockChange(Level level, BlockPos blockPos, BlockState newBlockState, BlockState oldBlockState, int flags, int maxUpdateDepth) {

    }

    public void onPlayerLeftClickBlock(ServerPlayerGameMode serverPlayerGameMode, BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight) {

    }
}