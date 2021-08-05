package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    private static ServerLevel level = null;

    @Inject(method = "read", at = @At(value = "HEAD"))
    private static void captureWorld(ServerLevel serverLevel, StructureManager structureManager, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<ProtoChunk> cir) {
        level = serverLevel;
    }

    @Redirect(method = "read", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/LevelChunkSection"))
    private static LevelChunkSection redirectInit(int i) {
        LevelChunkSection section = new LevelChunkSection(i);
        ((LevelChunkSectionInterface)section).initValues(null, level, false);
        return section;
    }

}
