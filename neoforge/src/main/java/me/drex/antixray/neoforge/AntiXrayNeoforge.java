package me.drex.antixray.neoforge;

import me.drex.antixray.common.AntiXray;
import me.drex.antixray.common.util.Platform;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class AntiXrayNeoforge extends AntiXray {

    public AntiXrayNeoforge() {
        super(Platform.FORGE);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String getConfigFileName() {
        return "antixray-neoforge.toml";
    }
}
