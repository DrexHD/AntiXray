package me.drex.antixray.mixin;

import me.drex.antixray.interfaces.IChunkSection;
import me.drex.antixray.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {

    @Redirect(
            method = "replaceMissingSections",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/level/chunk/LevelChunkSection"
            )

    )
    private static LevelChunkSection addPresetValues(int y, Registry<Biome> registry, LevelHeightAccessor accessor, Registry<Biome> registry2, LevelChunkSection[] sections) {
        final LevelChunkSection section = new LevelChunkSection(y, registry);
        ((IChunkSection) section).addBlockPresets(Util.getLevel(accessor));
        return section;
    }
}
