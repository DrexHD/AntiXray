package me.drex.antixray;

import dev.architectury.injectables.annotations.ExpectPlatform;
import me.drex.antixray.config.Config;
import me.drex.antixray.util.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class AntiXray {
    public static final String MOD_ID = "antixray";
    public static final Logger LOGGER = LogManager.getLogger();
    private static Platform platform;

    public static void onInit(Platform platform) {
        AntiXray.platform = platform;
        Config.loadConfig(getConfigDirectory().resolve("antixray.toml").toFile());
    }

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

}
