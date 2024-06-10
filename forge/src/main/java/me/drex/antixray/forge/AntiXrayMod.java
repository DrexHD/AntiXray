package me.drex.antixray.forge;

import me.drex.antixray.common.AntiXray;
import net.minecraftforge.fml.common.Mod;

@Mod(AntiXray.MOD_ID)
public class AntiXrayMod {

    private final AntiXrayForge mod;

    public AntiXrayMod() {
        mod = new AntiXrayForge();
    }

}
