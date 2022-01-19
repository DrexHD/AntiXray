package me.drex.antixray.config;

import com.moandjiezana.toml.Toml;
import me.drex.antixray.AntiXray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class Config {
    public static Toml toml;

    public static void loadConfig(File file) {
        if (!file.exists()) {
            try {
                Files.copy(Objects.requireNonNull(Config.class.getResourceAsStream("/data/antixray.toml")), file.toPath());
            } catch (IOException e) {
                AntiXray.LOGGER.error("Couldn't create default config", e);
                return;
            }
        }
        toml = new Toml().read(file);
    }
}