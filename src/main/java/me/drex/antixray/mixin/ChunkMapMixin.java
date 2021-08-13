package me.drex.antixray.mixin;

import me.drex.antixray.util.ProtoChunkInterface;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @Shadow @Final private ServerLevel level;

    // Add ServerLevel instance to ProtoChunk
    @Redirect(method = "lambda$scheduleChunkLoad$14", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/ProtoChunk"))
    public ProtoChunk initValues(ChunkPos chunkPos, UpgradeData upgradeData) {
        ProtoChunk protoChunk = new ProtoChunk(chunkPos, upgradeData);
        ((ProtoChunkInterface)protoChunk).initValues(this.level);
        return protoChunk;
    }

}
