package me.drex.antixray.mixin.accessor;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.Palette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelChunkSection.class)
public interface LevelChunkSectionAccessor {

    @Accessor("GLOBAL_BLOCKSTATE_PALETTE")
    static Palette<BlockState> getPalette() {
        throw new AssertionError();
    }

}
