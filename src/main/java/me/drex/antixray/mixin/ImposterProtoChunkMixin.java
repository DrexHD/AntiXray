package me.drex.antixray.mixin;

import me.drex.antixray.util.ProtoChunkInterface;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkMixin extends ProtoChunk {

    public ImposterProtoChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData) {
        super(chunkPos, upgradeData);
    }

    // Add ServerLevel instance to ProtoChunk
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/chunk/ImposterProtoChunk;wrapped:Lnet/minecraft/world/level/chunk/LevelChunk;"))
    public void initValues(LevelChunk levelChunk, CallbackInfo ci) {
        ((ProtoChunkInterface)this).initValues(levelChunk.getLevel());
    }

}
