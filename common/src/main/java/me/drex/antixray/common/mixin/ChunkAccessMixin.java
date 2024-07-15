package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.drex.antixray.common.util.Arguments;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;replaceMissingSections(Lnet/minecraft/core/Registry;[Lnet/minecraft/world/level/chunk/LevelChunkSection;)V"))
    public void setChunkAccessInstanceArgument(Registry<Biome> registry, LevelChunkSection[] chunkSections, Operation<Void> original) {
        // set argument
        var previous = Arguments.CHUNK_ACCESS.get();
        Arguments.CHUNK_ACCESS.set((ChunkAccess) (Object) this);
        try {
            original.call(registry, chunkSections);
        } finally {
            Arguments.CHUNK_ACCESS.set(previous);
        }
    }

    @WrapOperation(
        method = "replaceMissingSections",
        at = @At(
            value = "NEW", target = "(Lnet/minecraft/core/Registry;)Lnet/minecraft/world/level/chunk/LevelChunkSection;"
        )
    )
    private static LevelChunkSection setChunkSectionIndexArgument(Registry<Biome> arg, Operation<LevelChunkSection> original, @Local int i) {
        // custom arguments
        ChunkAccess thisChunkAccess = Arguments.CHUNK_ACCESS.get(); // simulate instance method

        // set argument
        var previous = Arguments.CHUNK_SECTION_INDEX.get();
        Arguments.CHUNK_SECTION_INDEX.set(thisChunkAccess.levelHeightAccessor.getSectionYFromSectionIndex(i));
        try {
            return original.call(arg);
        } finally {
            Arguments.CHUNK_SECTION_INDEX.set(previous);
        }
    }

}
