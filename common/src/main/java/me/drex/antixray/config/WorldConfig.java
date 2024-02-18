package me.drex.antixray.config;

import com.moandjiezana.toml.Toml;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.antixray.AntiXray;
import me.drex.antixray.util.EngineMode;
import me.drex.antixray.util.controller.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WorldConfig {
    public boolean enabled = false;
    public EngineMode engineMode = EngineMode.HIDE;
    public int maxBlockHeight = 64;
    public int updateRadius = 2;
    public boolean lavaObscures = false;
    public Set<Block> hiddenBlocks = new HashSet<>();
    public Set<Block> replacementBlocks = new HashSet<>();

    public WorldConfig(ResourceLocation location) {
        Toml defaultToml = Config.toml;
        // Load default values
        this.loadValues(defaultToml);
        Toml worldToml = defaultToml.getTable(location.getPath());
        // Overwrite default configurations with world specific configurations
        if (worldToml != null) {
            this.loadValues(worldToml);
        } else {
            this.loadValues(defaultToml.getTable("'" + location + "'"));
            this.loadValues(defaultToml.getTable("\"" + location + "\""));
        }
    }

    private void loadValues(Toml toml) {
        if (toml == null) return;
        if (toml.contains("enabled")) this.enabled = toml.getBoolean("enabled");
        if (toml.contains("engineMode")) {
            EngineMode mode = EngineMode.getById(Math.toIntExact(toml.getLong("engineMode")));
            if (mode != null) {
                this.engineMode = mode;
            }
        }
        if (toml.contains("maxBlockHeight")) {
            this.maxBlockHeight = Math.toIntExact(toml.getLong("maxBlockHeight")) >> 4 << 4;
        }
        if (toml.contains("updateRadius")) this.updateRadius = Math.toIntExact(toml.getLong("updateRadius"));
        if (toml.contains("lavaObscures")) this.lavaObscures = toml.getBoolean("lavaObscures");
        if (toml.contains("hiddenBlocks")) this.hiddenBlocks = parseBlocks(toml.getList("hiddenBlocks"));
        if (toml.contains("replacementBlocks")) this.replacementBlocks = parseBlocks(toml.getList("replacementBlocks"));

    }

    private Set<Block> parseBlocks(List<String> blocks) {
        Set<Block> result = new HashSet<>();
        for (String blockId : blocks) {
            try {
                StringReader stringReader = new StringReader(blockId);
                boolean isTag = false;
                if (stringReader.canRead() && stringReader.peek() == '#') {
                    isTag = true;
                    stringReader.skip();
                }
                ResourceLocation location = ResourceLocation.read(stringReader);
                if (isTag) {
                    TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, location);
                    Optional<HolderSet.Named<Block>> optional = BuiltInRegistries.BLOCK.getTag(tagKey);
                    if (optional.isPresent()) {
                        for (Holder<Block> holder : optional.get()) {
                            result.add(holder.value());
                        }
                    } else {
                        AntiXray.LOGGER.warn("Unknown block tag id: \"{}\"", blockId);
                    }
                } else {
                    Optional<Block> optional = BuiltInRegistries.BLOCK.getOptional(location);
                    optional.ifPresentOrElse(
                        result::add,
                        () -> AntiXray.LOGGER.warn("Unknown block id: \"{}\"", blockId)
                    );
                }
            } catch (CommandSyntaxException exception) {
                AntiXray.LOGGER.warn("Failed to parse block: \"{}\"", blockId, exception);
            }
        }
        return result;
    }

    public ChunkPacketBlockController createChunkPacketBlockController(Level level) {
        if (!this.enabled) return DisabledChunkPacketBlockController.NO_OPERATION_INSTANCE;
        return switch (engineMode) {
            case HIDE ->
                new HideChunkPacketBlockController(level, hiddenBlocks, maxBlockHeight, updateRadius, lavaObscures);
            case OBFUSCATE ->
                new ObfuscateChunkPacketBlockController(level, replacementBlocks, hiddenBlocks, maxBlockHeight, updateRadius, lavaObscures);
            case OBFUSCATE_LAYER ->
                new ObfuscateLayerChunkPacketBlockController(level, replacementBlocks, hiddenBlocks, maxBlockHeight, updateRadius, lavaObscures);
        };
    }

}
