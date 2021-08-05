package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements ChunkAccess {

    @Shadow
    @Final
    private Level level;

    @Redirect(method = "setBlockState", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/LevelChunkSection"))
    public LevelChunkSection redirectInit(int i) {
        LevelChunkSection section = new LevelChunkSection(i);
        ((LevelChunkSectionInterface) section).initValues((LevelChunk) (Object) this, this.level, true);
        return section;
    }

    @Redirect(method = "replaceWithPacketData", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/LevelChunkSection"))
    public LevelChunkSection redirectLevelChunkSectionInitInReplaceWithPacketData(int i) {
        LevelChunkSection section = new LevelChunkSection(i);
        ((LevelChunkSectionInterface) section).initValues((LevelChunk) (Object) this, this.level, true);
        return section;
    }

    @Override
    public LevelChunkSection getOrCreateSection(int i) {
        LevelChunkSection[] levelChunkSections = this.getSections();
        if (levelChunkSections[i] == LevelChunk.EMPTY_SECTION) {
            LevelChunkSection section = new LevelChunkSection(this.getSectionYFromSectionIndex(i));
            ((LevelChunkSectionInterface) section).initValues(this, this.level, true);
            levelChunkSections[i] = section;
        }
        return levelChunkSections[i];
    }
}
