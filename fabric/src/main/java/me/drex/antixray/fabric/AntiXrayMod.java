package me.drex.antixray.fabric;

import net.fabricmc.api.ModInitializer;

public class AntiXrayMod implements ModInitializer {

    private AntiXrayFabric mod;

    @Override
    public void onInitialize() {
        this.mod = new AntiXrayFabric();
    }
}
