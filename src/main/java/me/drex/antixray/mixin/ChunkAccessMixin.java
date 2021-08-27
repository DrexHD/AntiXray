package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkAccess.class)
public interface ChunkAccessMixin extends LevelHeightAccessor {

    @Shadow
    LevelChunkSection[] getSections();

    /**
     * @author Drex
     * @reason Initialize values for LevelChunkSection
     */
    @Overwrite
    default LevelChunkSection getOrCreateSection(int i) {
        LevelChunkSection[] chunkSection = this.getSections();
        if (chunkSection[i] == LevelChunk.EMPTY_SECTION) {
            chunkSection[i] = new LevelChunkSection(this.getSectionYFromSectionIndex(i));
            ((LevelChunkSectionInterface) chunkSection[i]).initValues((ChunkAccess) this);
        }

        return chunkSection[i];
    }
}
