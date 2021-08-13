package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements ChunkAccess {

    @Shadow
    @Final
    Level level;

    @Inject(method = "setBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/chunk/LevelChunk;sections:[Lnet/minecraft/world/level/chunk/LevelChunkSection;", ordinal = 1, opcode = Opcodes.GETFIELD), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initializeChunkSection(BlockPos blockPos, BlockState blockState, boolean bl, CallbackInfoReturnable<BlockState> cir, int blockY, int sectionIndex, LevelChunkSection levelChunkSection) {
        ((LevelChunkSectionInterface) levelChunkSection).initValues(this.level, true);
    }

}
