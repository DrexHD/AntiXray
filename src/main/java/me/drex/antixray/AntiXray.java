package me.drex.antixray;

import io.github.slimjar.app.builder.ApplicationBuilder;
import me.drex.antixray.config.Config;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class AntiXray implements DedicatedServerModInitializer {

    private static MinecraftServer minecraftServer;
    public static Logger LOGGER = LogManager.getLogger();

    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    @Override
    public void onInitializeServer() {
        downloadDependencies();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> minecraftServer = server);
        Config.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("antixray.toml").toFile());
    }

    private void downloadDependencies() {
        LOGGER.info("Downloading dependencies, this might take a couple of seconds...");
        try {
            ApplicationBuilder.appending("FabricAntiXray")
                    .logger((message, args) -> LOGGER.info(message.replaceAll("\\{\\d+}", "{}"), args))
                    .downloadDirectoryPath(FabricLoader.getInstance().getConfigDir().resolve("libraries"))
                    .build();
        } catch (URISyntaxException | ReflectiveOperationException | NoSuchAlgorithmException | IOException e) {
            LOGGER.error("Failed to download dependencies:", e);
        }
    }


}
