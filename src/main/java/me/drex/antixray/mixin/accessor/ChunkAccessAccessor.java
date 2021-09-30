package me.drex.antixray.mixin.accessor;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkAccess.class)
public interface ChunkAccessAccessor {

    @Accessor("levelHeightAccessor")
    LevelHeightAccessor getLevelHeightAccessor();

}
