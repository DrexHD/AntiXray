package me.drex.antixray;

import me.drex.antixray.config.Config;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AntiXray implements DedicatedServerModInitializer {

    private static MinecraftServer minecraftServer;
    public static Logger LOGGER = LogManager.getLogger();

    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            minecraftServer = server;
        });
        Config.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("antixray.toml").toFile());
    }


}
