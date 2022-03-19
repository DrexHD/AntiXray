package me.drex.antixray.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;

public class AntiXrayMod implements DedicatedServerModInitializer {

    private AntiXrayFabric mod;

    @Override
    public void onInitializeServer() {
        this.mod = new AntiXrayFabric();
    }
}
