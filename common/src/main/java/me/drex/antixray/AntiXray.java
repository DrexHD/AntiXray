package me.drex.antixray;

import me.drex.antixray.config.Config;
import me.drex.antixray.util.Platform;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public abstract class AntiXray {
    public static final String MOD_ID = "antixray";
    public static final Logger LOGGER = LogManager.getLogger();
    public static AntiXray INSTANCE;
    private static Platform platform;

    public AntiXray(Platform platform) {
        AntiXray.platform = platform;
        Config.loadConfig(getConfigDirectory().resolve("antixray.toml").toFile());
        LOGGER.info("Successfully initialized {} on {}", MOD_ID, platform.name().toLowerCase());
        INSTANCE = this;
    }

    public abstract boolean canBypassXray(ServerPlayer player);

    public abstract Path getConfigDirectory();

}
