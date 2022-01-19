package me.drex.antixray.mixin;

import me.drex.antixray.config.WorldConfig;
import me.drex.antixray.interfaces.ILevel;
import me.drex.antixray.util.ChunkPacketBlockController;
import me.drex.antixray.util.ChunkPacketBlockControllerAntiXray;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.Executor;

@Mixin(Level.class)
public abstract class LevelMixin implements ILevel, LevelAccessor {

    @Unique
    public ChunkPacketBlockController chunkPacketBlockController;

    @Unique
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
    public void initValues(final Executor executor) {
        this.worldConfig = new WorldConfig(this.dimension.location());
        this.chunkPacketBlockController = this.worldConfig.enabled ? new ChunkPacketBlockControllerAntiXray((Level) (Object) this, executor) : ChunkPacketBlockControllerAntiXray.NO_OPERATION_INSTANCE;
    }

    @Override
    public ChunkPacketBlockController getChunkPacketBlockController() {
        return this.chunkPacketBlockController;
    }

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onBlockChange(final BlockPos blockPos, final BlockState blockState, final int flags, final int maxUpdateDepth, final CallbackInfoReturnable<Boolean> cir, final LevelChunk levelChunk, final Block block) {
        final BlockState oldState = levelChunk.getBlockState(blockPos);
        this.chunkPacketBlockController.onBlockChange((Level) (Object) this, blockPos, blockState, oldState, flags, maxUpdateDepth);
    }
}
