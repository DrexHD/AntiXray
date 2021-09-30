package me.drex.antixray.config;

import com.moandjiezana.toml.Toml;
import me.drex.antixray.util.ChunkPacketBlockControllerAntiXray;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorldConfig {

    public boolean enabled = false;
    public ChunkPacketBlockControllerAntiXray.EngineMode engineMode = ChunkPacketBlockControllerAntiXray.EngineMode.HIDE;
    public int maxBlockHeight = 64;
    public int updateRadius = 2;
    public boolean lavaObscures = false;
    public boolean usePermission = false;
    public List<String> hiddenBlocks = new ArrayList<>();
    public List<String> replacementBlocks = new ArrayList<>();

    public WorldConfig(ResourceLocation location) {
        Toml defaultToml = Config.toml;
        // Load default values
        this.loadValues(defaultToml);
        Toml worldToml = defaultToml.getTable(location.getPath());
        // Overwrite default configurations with world specific configurations
        if (worldToml != null) {
            this.loadValues(worldToml);
        }
    }

    private void loadValues(@NotNull Toml toml) {
        if (toml.contains("enabled")) this.enabled = toml.getBoolean("enabled");
        if (toml.contains("engineMode")) {
            ChunkPacketBlockControllerAntiXray.EngineMode mode = ChunkPacketBlockControllerAntiXray.EngineMode.getById(Math.toIntExact(toml.getLong("engineMode")));
            if (mode != null) this.engineMode = mode;
        }
        if (toml.contains("maxBlockHeight")) {
            this.maxBlockHeight = Math.toIntExact(toml.getLong("maxBlockHeight"));
        }
        if (toml.contains("updateRadius")) this.updateRadius = Math.toIntExact(toml.getLong("updateRadius"));
        if (toml.contains("lavaObscures")) this.lavaObscures = toml.getBoolean("lavaObscures");
        if (toml.contains("usePermission")) this.usePermission = toml.getBoolean("usePermission");
        if (toml.contains("hiddenBlocks")) this.hiddenBlocks = toml.getList("hiddenBlocks");
        if (toml.contains("replacementBlocks")) this.replacementBlocks = toml.getList("replacementBlocks");
    }

}
