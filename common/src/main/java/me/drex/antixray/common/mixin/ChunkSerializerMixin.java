package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import me.drex.antixray.common.util.Arguments;
import me.drex.antixray.common.util.Util;
import net.minecraft.core.IdMap;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SerializableChunkData.class, priority = 1500)
public abstract class ChunkSerializerMixin {

    @WrapOperation(
        method = "parse",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/core/IdMap;Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;)Lnet/minecraft/world/level/chunk/PalettedContainer;",
            ordinal = 0
        )
    )
    private static PalettedContainer<BlockState> setPresetValuesArgument(
        IdMap<BlockState> idMap, Object defaultValue, PalettedContainer.Strategy strategy,
        Operation<PalettedContainer<BlockState>> original, LevelHeightAccessor levelHeightAccessor,
        @Local(ordinal = 1) int sectionIndex
    ) {
        Level level = Util.getLevel(levelHeightAccessor);
        final BlockState[] presetValues = Util.getBlockController(level).getPresetBlockStates(level, sectionIndex << 4);

        var previous = Arguments.PRESET_VALUES.get();
        Arguments.PRESET_VALUES.set(presetValues);
        try {
            return original.call(idMap, defaultValue, strategy);
        } finally {
            Arguments.PRESET_VALUES.set(previous);
        }
    }

    @WrapOperation(
        method = "parse",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
            ordinal = 2
        )
    )
    private static DataResult<PalettedContainer<BlockState>> setPresetValuesArgument(
        Codec<PalettedContainer<BlockState>> instance, DynamicOps<Tag> dynamicOps, Object data,
        Operation<DataResult<PalettedContainer<BlockState>>> original, LevelHeightAccessor levelHeightAccessor,
        @Local(ordinal = 1) int sectionIndex
    ) {
        Level level = Util.getLevel(levelHeightAccessor);
        final BlockState[] presetValues = Util.getBlockController(level).getPresetBlockStates(level, sectionIndex << 4);

        var previous = Arguments.PRESET_VALUES.get();
        Arguments.PRESET_VALUES.set(presetValues);
        try {
            return original.call(instance, dynamicOps, data);
        } finally {
            Arguments.PRESET_VALUES.set(previous);
        }
    }
}
