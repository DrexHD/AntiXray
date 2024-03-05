package me.drex.antixray.mixin;

import me.drex.antixray.interfaces.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Shadow
    @Final
    Level level;

    @Shadow
    public abstract BlockState getBlockState(BlockPos blockPos);

    @Inject(
        method = "setBlockState",
        at = @At("HEAD")
    )
    public void onBlockChange(BlockPos blockPos, BlockState blockState, boolean bl, CallbackInfoReturnable<Block> cir) {
        if (this.level instanceof ServerLevel serverLevel) {
            final BlockState oldState = this.getBlockState(blockPos);
            ((ILevel) this.level).getChunkPacketBlockController().onBlockChange(serverLevel, blockPos, blockState, oldState);
        }
    }

}
