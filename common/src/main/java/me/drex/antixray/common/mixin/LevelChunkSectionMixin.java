package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.antixray.common.util.Arguments;
import me.drex.antixray.common.util.Util;
import me.drex.antixray.common.util.controller.ChunkPacketBlockController;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin {
    @WrapOperation(
        method = "<init>(Lnet/minecraft/core/Registry;)V",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/core/IdMap;Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;)Lnet/minecraft/world/level/chunk/PalettedContainer;",
            ordinal = 0
        )
    )
    public PalettedContainer<BlockState> setPresetValuesArgument(IdMap<BlockState> idMap, Object defaultValue, PalettedContainer.Strategy strategy, Operation<PalettedContainer<BlockState>> original) {
        if (((Object) this).getClass() != LevelChunkSection.class) {
            // Compatibility with Flywheel's VirtualChunkSection
            return original.call(idMap, defaultValue, strategy);
        }
        // custom arguments
        ChunkAccess chunkAccess = Arguments.CHUNK_ACCESS.get();
        Integer chunkSectionIndex = Arguments.CHUNK_SECTION_INDEX.get();

        Level level = Util.getLevel(chunkAccess.levelHeightAccessor);
        ChunkPacketBlockController controller = Util.getBlockController(level);
        if (controller != null) {
            final BlockState[] presetValues = controller.getPresetBlockStates(level, chunkSectionIndex << 4);
            var previous = Arguments.PRESET_VALUES.get();
            Arguments.PRESET_VALUES.set(presetValues);
            try {
                return original.call(idMap, defaultValue, strategy);
            } finally {
                Arguments.PRESET_VALUES.set(previous);
            }
        }
        return original.call(idMap, defaultValue, strategy);
    }

    @WrapOperation(
        method = "<init>(Lnet/minecraft/core/Registry;)V",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/core/IdMap;Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;)Lnet/minecraft/world/level/chunk/PalettedContainer;",
            ordinal = 1
        )
    )
    public PalettedContainer<BlockState> setPacketInfoArgumentNull(IdMap<BlockState> idMap, Object defaultValue, PalettedContainer.Strategy strategy, Operation<PalettedContainer<BlockState>> original) {
        var previous = Arguments.PACKET_INFO.get();
        Arguments.PACKET_INFO.remove();
        try {
            return original.call(idMap, defaultValue, strategy);
        } finally {
            Arguments.PACKET_INFO.set(previous);
        }
    }

    @WrapOperation(
        method = "write",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/PalettedContainerRO;write(Lnet/minecraft/network/FriendlyByteBuf;)V"
        )
    )
    public void setPacketInfoArgumentNull(PalettedContainerRO<Holder<Biome>> instance, FriendlyByteBuf buf, Operation<Void> original) {
        var previous = Arguments.PACKET_INFO.get();
        Arguments.PACKET_INFO.remove();
        try {
            original.call(instance, buf);
        } finally {
            Arguments.PACKET_INFO.set(previous);
        }
    }
}
