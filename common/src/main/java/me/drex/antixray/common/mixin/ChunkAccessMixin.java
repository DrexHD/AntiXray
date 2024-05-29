package me.drex.antixray.common.mixin;

import me.drex.antixray.common.interfaces.IChunkSection;
import me.drex.antixray.common.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {

    @Unique
    private static void replaceMissingSections(LevelHeightAccessor accessor, Registry<Biome> registry, LevelChunkSection[] levelChunkSections) {
        // [VanillaCopy] Re-added LevelHeightAccessor, which was removed from replaceMissingSections in 23w16a (1.20)
        for (int i = 0; i < levelChunkSections.length; i++) {
            if (levelChunkSections[i] == null) {
                levelChunkSections[i] = new LevelChunkSection(registry);
                Level level = Util.getLevel(accessor);
                if (level instanceof ServerLevel serverLevel) {
                    ((IChunkSection) levelChunkSections[i]).init(accessor.getSectionYFromSectionIndex(i) << 4);
                    ((IChunkSection) levelChunkSections[i]).addBlockPresets(serverLevel);
                }
            }
        }
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/ChunkAccess;replaceMissingSections(Lnet/minecraft/core/Registry;[Lnet/minecraft/world/level/chunk/LevelChunkSection;)V"
        )
    )
    private void initializeChunkSection(Registry<Biome> registry, LevelChunkSection[] levelChunkSections, ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor) {
        replaceMissingSections(levelHeightAccessor, registry, levelChunkSections);
    }

}
