package me.drex.antixray.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.Nullable;

public class ChunkPacketBlockController {

    public static final ChunkPacketBlockController NO_OPERATION_INSTANCE = new ChunkPacketBlockController();

    protected ChunkPacketBlockController() {

    }

    public BlockState[] getPresetBlockStates(Level level, LevelChunkSection chunkSection) {
        return null;
    }

    public boolean shouldModify(ServerPlayer player, LevelChunk chunk) {
        return false;
    }

    public ChunkPacketInfo<BlockState> getChunkPacketInfo(ClientboundLevelChunkPacket chunkPacket, LevelChunk chunk) {
        return null;
    }

    public void modifyBlocks(ClientboundLevelChunkPacket chunkPacket, ChunkPacketInfo<BlockState> chunkPacketInfo) {
        ((ClientboundLevelChunkPacketInterface)chunkPacket).setReady();
    }

    public void onBlockChange(Level level, BlockPos blockPos, BlockState newBlockState, @Nullable BlockState oldBlockState) {

    }

    public void onPlayerLeftClickBlock(ServerPlayerGameMode serverPlayerGameMode, BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight) {

    }
}
