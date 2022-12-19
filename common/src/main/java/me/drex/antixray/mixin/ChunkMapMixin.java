package me.drex.antixray.mixin;

import me.drex.antixray.AntiXray;
import me.drex.antixray.interfaces.IChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Shadow
    @Final
    private ThreadedLevelLightEngine lightEngine;
    @Unique
    private static final Object NON_NULL = new Object();

    // playerLoadedChunk "local" variables
    @Unique
    private ServerPlayer antixray$playerLoadedChunk$ServerPlayer;
    @Unique
    private LevelChunk antixray$playerLoadedChunk$LevelChunk;

    // resendChunk "local" variables
    @Unique
    Map<Boolean, ClientboundLevelChunkWithLightPacket> antixray$resendChunk$refreshPackets;
    @Unique
    private LevelChunk antixray$resendChunk$LevelChunk;
    @Unique
    private ServerPlayer antixray$resendChunk$ServerPlayer;

    /**
     * The following mixins update the type of the second argument of playerLoadedChunk from
     * MutableObject<ClientboundLevelChunkWithLightPacket> to MutableObject<HashMap<Boolean, ClientboundLevelChunkWithLightPacket>>
     * to store obfuscated and non obfuscated packets!
     */
    @Inject(
            method = "playerLoadedChunk",
            at = @At("HEAD")
    )
    public void playerLoadedChunk$captureLocal(ServerPlayer serverPlayer, MutableObject mutableObject, LevelChunk levelChunk, CallbackInfo ci) {
        antixray$playerLoadedChunk$ServerPlayer = serverPlayer;
        antixray$playerLoadedChunk$LevelChunk = levelChunk;
    }

    @Redirect(
            method = "playerLoadedChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/commons/lang3/mutable/MutableObject;getValue()Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    public Object changeMutableObjectType(MutableObject mutableObject) {
        // Initialize mutable object with updated type
        if (mutableObject.getValue() == null) {
            mutableObject.setValue(new HashMap<>());
        }

        // Return non-null object to prevent original setValue
        return NON_NULL;
    }

    @Redirect(
            method = "playerLoadedChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/commons/lang3/mutable/MutableObject;getValue()Ljava/lang/Object;",
                    ordinal = 1
            )
    )
    public Object updateChunkPacket(MutableObject mutableObject) {
        Boolean shouldModify = AntiXray.INSTANCE.canBypassXray(antixray$playerLoadedChunk$ServerPlayer);
        MutableObject<HashMap<Boolean, ClientboundLevelChunkWithLightPacket>> updatedMutableObject = mutableObject;
        return updatedMutableObject.getValue().computeIfAbsent(shouldModify, (s) -> createChunkPacket(antixray$playerLoadedChunk$LevelChunk, s));
    }

    private ClientboundLevelChunkWithLightPacket createChunkPacket(LevelChunk levelChunk, boolean shouldModify) {
        ClientboundLevelChunkWithLightPacket chunkWithLightPacket = new ClientboundLevelChunkWithLightPacket(levelChunk, this.lightEngine, null, null, true);
        ((IChunkPacket) chunkWithLightPacket).modifyPacket(shouldModify);
        return chunkWithLightPacket;
    }

}
