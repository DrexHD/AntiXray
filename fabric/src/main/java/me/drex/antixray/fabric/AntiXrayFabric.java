package me.drex.antixray.fabric;

import me.drex.antixray.AntiXray;
import me.drex.antixray.util.Platform;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

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

    @Override
    public boolean canBypassXray(ServerPlayer player) {
        return Permissions.check(player, "antixray.bypass");
    }
}
