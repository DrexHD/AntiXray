package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin implements ChunkAccess {

    @Shadow
    @Final
    private LevelHeightAccessor levelHeightAccessor;

    @Override
    public LevelChunkSection getOrCreateSection(int i) {
        LevelChunkSection[] levelChunkSections = this.getSections();
        if (levelChunkSections[i] == LevelChunk.EMPTY_SECTION) {
            LevelChunkSection section = new LevelChunkSection(this.getSectionYFromSectionIndex(i));
            LevelHeightAccessor heightAccessor = this.levelHeightAccessor;
            ((LevelChunkSectionInterface) section).initValues(this, heightAccessor instanceof Level ? (Level) heightAccessor : null, true);
            levelChunkSections[i] = section;
        }
        return levelChunkSections[i];
    }
}
