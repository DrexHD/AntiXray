package me.drex.antixray.mixin;

import com.mojang.serialization.Codec;
import me.drex.antixray.interfaces.IPalettedContainer;
import me.drex.antixray.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {

    @SuppressWarnings("unchecked")
    @Inject(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;checkConsistencyWithBlocks(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/LevelChunkSection;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void addPresetValues(ServerLevel serverLevel, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<ProtoChunk> cir, ChunkPos chunkPos2, UpgradeData upgradeData, boolean bl, ListTag listTag, int i, LevelChunkSection[] levelChunkSections, boolean bl2, ChunkSource chunkSource, LevelLightEngine levelLightEngine, Registry registry, Codec codec, int j, CompoundTag compoundTag2, int k, int l, PalettedContainer<BlockState> palettedContainer, PalettedContainer<Biome> palettedContainer2, LevelChunkSection levelChunkSection) {
        final IPalettedContainer<BlockState> container = (IPalettedContainer<BlockState>) palettedContainer;
        final BlockState[] presetValues = Util.getBlockController(serverLevel).getPresetBlockStates(serverLevel, k << 4);
        if (compoundTag2.contains("block_states", 10)) {
            container.addPresetValuesWithEntries(presetValues);
        } else {
            container.addPresetValues(presetValues);
        }
    }
}
