package me.drex.antixray.neoforge;

import me.drex.antixray.common.AntiXray;
import net.neoforged.fml.common.Mod;

@Mod(AntiXray.MOD_ID)
public class AntiXrayMod {

    private final AntiXrayNeoforge mod;

    public AntiXrayMod() {
        mod = new AntiXrayNeoforge();
    }

}
