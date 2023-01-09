package me.drex.antixray.fabric;

import me.drex.antixray.AntiXray;
import me.drex.antixray.util.Platform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class AntiXrayFabric extends AntiXray {

    public AntiXrayFabric() {
        super(Platform.FABRIC);
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String getConfigFileName() {
        return "antixray-fabric.toml";
    }
}
