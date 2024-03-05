package me.drex.antixray.mixin;

import me.drex.antixray.config.WorldConfig;
import me.drex.antixray.interfaces.ILevel;
import me.drex.antixray.util.controller.ChunkPacketBlockController;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public abstract class LevelMixin implements ILevel, LevelAccessor {

    @Unique
    public ChunkPacketBlockController chunkPacketBlockController;

    @Shadow
    @Final
    private ResourceKey<Level> dimension;

    @Shadow
    public abstract BlockState getBlockState(BlockPos blockPos);

    @Override
    public void initValues() {
        if ((Object) this instanceof ServerLevel serverLevel) {
            WorldConfig worldConfig = new WorldConfig(this.dimension.location());
            this.chunkPacketBlockController = worldConfig.createChunkPacketBlockController(serverLevel);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public ChunkPacketBlockController getChunkPacketBlockController() {
        return this.chunkPacketBlockController;
    }
}
