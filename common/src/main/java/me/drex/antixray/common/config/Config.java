package me.drex.antixray.common.config;

import com.moandjiezana.toml.Toml;
import me.drex.antixray.common.AntiXray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Config {
    public static Toml toml;

    public static void loadConfig(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.copy(Objects.requireNonNull(Config.class.getResourceAsStream("/" + AntiXray.INSTANCE.getConfigFileName())), path);
            } catch (IOException e) {
                AntiXray.LOGGER.error("Failed to create default config", e);
                return;
            }
        }
        try {
            toml = new Toml().read(Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read antixray config file", e);
        }
    }
}