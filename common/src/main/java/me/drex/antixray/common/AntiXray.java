package me.drex.antixray.common;

import me.drex.antixray.common.config.Config;
import me.drex.antixray.common.util.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public abstract class AntiXray {
    public static final String MOD_ID = "antixray";
    public static final Logger LOGGER = LogManager.getLogger();
    public static AntiXray INSTANCE;
    private Platform platform;

    public AntiXray(Platform platform) {
        INSTANCE = this;
        this.platform = platform;
        Config.loadConfig(getConfigDirectory().resolve("antixray.toml"));
        LOGGER.info("Successfully initialized {} on {}", MOD_ID, platform.name().toLowerCase());
    }

    public abstract Path getConfigDirectory();

    public abstract String getConfigFileName();

}
