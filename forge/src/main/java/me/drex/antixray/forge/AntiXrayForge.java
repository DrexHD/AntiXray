package me.drex.antixray.forge;

import me.drex.antixray.common.AntiXray;
import me.drex.antixray.common.util.Platform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class AntiXrayForge extends AntiXray {

    public AntiXrayForge() {
        super(Platform.FORGE);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String getConfigFileName() {
        return "antixray-forge.toml";
    }
}
