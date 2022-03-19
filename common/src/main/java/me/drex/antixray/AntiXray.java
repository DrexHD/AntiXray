package me.drex.antixray;

import me.drex.antixray.config.Config;
import me.drex.antixray.util.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public abstract class AntiXray {
    public static final String MOD_ID = "antixray";
    public static final Logger LOGGER = LogManager.getLogger();
    private static Platform platform;

    public AntiXray(Platform platform) {
        AntiXray.platform = platform;
        Config.loadConfig(getConfigDirectory().resolve("antixray.toml").toFile());
    }

    public abstract Path getConfigDirectory();

}
