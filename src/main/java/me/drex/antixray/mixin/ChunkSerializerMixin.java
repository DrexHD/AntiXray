package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import me.drex.antixray.util.ProtoChunkInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {

    // Initialize values for LevelChunkSection
    @Inject(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;getStates()Lnet/minecraft/world/level/chunk/PalettedContainer;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void initializeChunkSection(ServerLevel serverLevel, StructureManager structureManager, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<ProtoChunk> cir, ChunkGenerator chunkGenerator, BiomeSource biomeSource, CompoundTag levelCompoundTag, ChunkBiomeContainer chunkBiomeContainer, UpgradeData upgradeData, ProtoTickList<Block> blockTickList, ProtoTickList<Fluid> fluidTickList, boolean isLightOn, ListTag listTag, int sectionCount, LevelChunkSection[] levelChunkSections, boolean hasSkyLight, ChunkSource chunkSource, LevelLightEngine levelLightEngine, int j, CompoundTag sectionCompoundTag, int sectionY, LevelChunkSection levelChunkSection) {
        ((LevelChunkSectionInterface) levelChunkSection).initValues(serverLevel, false);
    }

    // Add ServerLevel instance to ProtoChunk
    @Inject(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ProtoChunk;setBiomes(Lnet/minecraft/world/level/chunk/ChunkBiomeContainer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void  initializeProtoChunk(ServerLevel serverLevel, StructureManager structureManager, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<ProtoChunk> cir, ChunkGenerator chunkGenerator, BiomeSource biomeSource, CompoundTag compoundTag2, ChunkBiomeContainer chunkBiomeContainer, UpgradeData upgradeData, ProtoTickList protoTickList, ProtoTickList protoTickList2, boolean bl, ListTag listTag, int i, LevelChunkSection levelChunkSections[], boolean bl2, ChunkSource chunkSource, LevelLightEngine levelLightEngine, long l, ChunkStatus.ChunkType chunkType, ProtoChunk protoChunk) {
        ((ProtoChunkInterface)protoChunk).initValues(serverLevel);
    }
}
