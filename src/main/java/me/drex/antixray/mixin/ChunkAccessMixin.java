package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {

    @Inject(
            method = "replaceMissingSections",
            at = @At("RETURN")
    )
    private static void initializeChunkSections(LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, LevelChunkSection[] levelChunkSections, CallbackInfo ci) {
        for (LevelChunkSection levelChunkSection : levelChunkSections) {
            ((LevelChunkSectionInterface) levelChunkSection).initValues((ChunkAccess) levelHeightAccessor);
        }
    }

}
