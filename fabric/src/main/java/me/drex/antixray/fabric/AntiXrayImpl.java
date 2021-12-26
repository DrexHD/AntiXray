package me.drex.antixray.fabric;

import me.drex.antixray.AntiXray;
import me.drex.antixray.util.Platform;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class AntiXrayImpl implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        AntiXray.onInit(Platform.FABRIC);
    }

    /**
     * Config directory implementation for {@link Platform#FABRIC}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

}
