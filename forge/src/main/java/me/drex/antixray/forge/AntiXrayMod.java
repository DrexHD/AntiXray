package me.drex.antixray.forge;

import me.drex.antixray.AntiXray;
import me.drex.antixray.util.Platform;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(AntiXray.MOD_ID)
public class AntiXrayMod {

    private final AntiXrayForge mod;

    public AntiXrayMod() {
        mod = new AntiXrayForge();
    }

}
