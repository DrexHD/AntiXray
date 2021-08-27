package me.drex.antixray.mixin;

import me.drex.antixray.config.WorldConfig;
import me.drex.antixray.util.ChunkPacketBlockController;
import me.drex.antixray.util.ChunkPacketBlockControllerAntiXray;
import me.drex.antixray.util.LevelInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Executor;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelInterface, LevelAccessor {

    public ChunkPacketBlockController chunkPacketBlockController;
    private WorldConfig worldConfig;
    @Shadow
    @Final
    private ResourceKey<Level> dimension;

    @Override
    public WorldConfig getWorldConfig() {
        return worldConfig;
    }

    @Shadow
    public abstract BlockState getBlockState(BlockPos blockPos);

    @Override
    public void initValues(Executor executor) {
        this.worldConfig = new WorldConfig(this.dimension.location());
        this.chunkPacketBlockController = worldConfig.enabled ? new ChunkPacketBlockControllerAntiXray((Level) (Object) this, executor) : ChunkPacketBlockControllerAntiXray.NO_OPERATION_INSTANCE;
    }

    @Override
    public ChunkPacketBlockController getChunkPacketBlockController() {
        return this.chunkPacketBlockController;
    }

    @Redirect(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    public BlockState onBlockChanged(LevelChunk levelChunk, BlockPos blockPos, BlockState blockState, boolean bl) {
        BlockState oldState = levelChunk.setBlockState(blockPos, blockState, bl);
        this.getChunkPacketBlockController().onBlockChange((Level) (Object) this, blockPos, blockState, oldState);
        return oldState;
    }
}
