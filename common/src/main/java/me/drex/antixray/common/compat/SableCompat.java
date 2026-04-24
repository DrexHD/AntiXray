package me.drex.antixray.common.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import me.drex.antixray.common.AntiXray;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class SableCompat {
    public static boolean IS_LOADED = AntiXray.INSTANCE.isModLoaded("sablecompanion");

    public static boolean isInPlotGrid(Level level, ChunkPos pos) {
        if (IS_LOADED) {
            return SableCompanion.INSTANCE.isInPlotGrid(level, pos);
        }
        return false;
    }
}
