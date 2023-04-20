package me.drex.antixray.mixin;

import com.mojang.serialization.Codec;
import me.drex.antixray.interfaces.IChunkSection;
import me.drex.antixray.interfaces.IPalettedContainer;
import me.drex.antixray.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
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

@Mixin(value = ChunkSerializer.class, priority = 1500)
public abstract class ChunkSerializerMixin {

    @Inject(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;checkConsistencyWithBlocks(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/chunk/LevelChunkSection;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void addPresetValues(ServerLevel serverLevel, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<ProtoChunk> cir, ChunkPos chunkPos2, UpgradeData upgradeData, boolean b, ListTag listTag, int i, LevelChunkSection[] levelChunkSections, boolean b2, ChunkSource chunkSource, LevelLightEngine levelLightEngine, Registry<Biome> registry, Codec<PalettedContainer<Holder<Biome>>> codec, boolean b3, int j, CompoundTag tag, int k, int l, PalettedContainer<BlockState> blockStatePalette) {
        ((IChunkSection) levelChunkSections[l]).init(serverLevel.getSectionYFromSectionIndex(l) << 4);
        final IPalettedContainer<BlockState> container = (IPalettedContainer<BlockState>) blockStatePalette;
        final BlockState[] presetValues = Util.getBlockController(serverLevel).getPresetBlockStates(serverLevel, k << 4);
        if (tag.contains("block_states", 10)) {
            container.addPresetValuesWithEntries(presetValues);
        } else {
            container.addPresetValues(presetValues);
        }
    }
}
