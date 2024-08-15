package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.antixray.common.util.Arguments;
import me.drex.antixray.common.util.ChunkPacketInfo;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> {
    @Unique
    private T[] antiXray$presetValues;

    @Shadow
    private volatile PalettedContainer.Data<T> data;
    @Shadow
    @Final
    private PalettedContainer.Strategy strategy;
    @Shadow
    @Final
    private IdMap<T> registry;

    @Shadow
    public abstract int onResize(int i, T object);

    @Shadow
    protected abstract PalettedContainer.Data<T> createOrReuseData(PalettedContainer.Data<T> data, int i);

    @Inject(
        method = "<init>(Lnet/minecraft/core/IdMap;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;Lnet/minecraft/world/level/chunk/PalettedContainer$Configuration;Lnet/minecraft/util/BitStorage;Ljava/util/List;)V",
        at = @At("TAIL")
    )
    private void addPresetValuesWithEntries(IdMap<T> idList, PalettedContainer.Strategy strategy, PalettedContainer.Configuration<T> configuration, BitStorage storage, List<T> paletteEntries, CallbackInfo ci) {
        //noinspection unchecked
        this.antiXray$presetValues = (T[]) Arguments.PRESET_VALUES.get();

        if (antiXray$presetValues != null && (configuration.factory() == PalettedContainer.Strategy.SINGLE_VALUE_PALETTE_FACTORY ? this.data.palette().valueFor(0) != Blocks.AIR.defaultBlockState() : configuration.factory() != PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY)) {
            // In 1.18 Mojang unfortunately removed code that already handled possible resize operations on read from disk for us
            // We readd this here but in a smarter way than it was before
            int maxSize = 1 << configuration.bits();

            for (T presetValue : antiXray$presetValues) {
                if (this.data.palette().getSize() >= maxSize) {
                    java.util.Set<T> allValues = new java.util.HashSet<>(paletteEntries);
                    allValues.addAll(Arrays.asList(antiXray$presetValues));
                    int newBits = Mth.ceillog2(allValues.size());

                    if (newBits > configuration.bits()) {
                        this.onResize(newBits, null);
                    }

                    break;
                }

                this.data.palette().idFor(presetValue);
            }
        }
    }

    @Inject(
        method = "<init>(Lnet/minecraft/core/IdMap;Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;)V"
        , at = @At("TAIL")
    )
    public void addPresetValuesInit(IdMap<T> idMap, Object object, PalettedContainer.Strategy strategy, CallbackInfo ci) {
        //noinspection unchecked
        this.antiXray$presetValues = (T[]) Arguments.PRESET_VALUES.get();
    }

    @Inject(
        method = "<init>(Lnet/minecraft/core/IdMap;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;Lnet/minecraft/world/level/chunk/PalettedContainer$Data;)V",
        at = @At("TAIL")
    )
    public void addPresetValuesInit(IdMap<T> idMap, PalettedContainer.Strategy strategy, PalettedContainer.Data<T> data, CallbackInfo ci) {
        //noinspection unchecked
        this.antiXray$presetValues = (T[]) Arguments.PRESET_VALUES.get();
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/chunk/PalettedContainer;)V",
        at = @At("TAIL")
    )
    public void addPresetValuesInit(PalettedContainer<T> palettedContainer, CallbackInfo ci) {
        //noinspection unchecked
        this.antiXray$presetValues = (T[]) Arguments.PRESET_VALUES.get();
    }

    @Redirect(
        method = "onResize",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/PalettedContainer;createOrReuseData(Lnet/minecraft/world/level/chunk/PalettedContainer$Data;I)Lnet/minecraft/world/level/chunk/PalettedContainer$Data;"
        )
    )
    private PalettedContainer.Data<T> addPresetValues(PalettedContainer<T> container, PalettedContainer.Data<T> data, int bits, int i, T object) {
        if (this.antiXray$presetValues != null && object != null && data.configuration().factory() == PalettedContainer.Strategy.SINGLE_VALUE_PALETTE_FACTORY) {
            int duplicates = 0;
            List<T> presetValues = Arrays.asList(this.antiXray$presetValues);
            duplicates += presetValues.contains(object) ? 1 : 0;
            duplicates += presetValues.contains(data.palette().valueFor(0)) ? 1 : 0;
            bits = Mth.ceillog2((1 << this.strategy.calculateBitsForSerialization(this.registry, 1 << bits)) + presetValues.size() - duplicates);
        }

        return this.createOrReuseData(data, bits);
    }

    @Redirect(
        method = "onResize",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/Palette;idFor(Ljava/lang/Object;)I"
        )
    )
    private int addPresetValues(Palette<T> palette, T object) {
        this.antiXray$addPresetValues();
        return object == null ? -1 : palette.idFor(object);
    }

    @Inject(
        method = "write",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/PalettedContainer$Data;write(Lnet/minecraft/network/FriendlyByteBuf;)V",
            shift = At.Shift.AFTER
        )
    )
    public void setPresetValues(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        // custom arguments
        ChunkPacketInfo<BlockState> chunkPacketInfo = Arguments.PACKET_INFO.get();
        Integer chunkSectionIndex = Arguments.CHUNK_SECTION_INDEX.get();

        if (chunkPacketInfo != null) {
            chunkPacketInfo.setPresetValues(chunkSectionIndex, (BlockState[]) this.antiXray$presetValues);
        }
    }

    @WrapOperation(
        method = "copy",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/chunk/PalettedContainer;)Lnet/minecraft/world/level/chunk/PalettedContainer;"
        )
    )
    // IdMap<T> idMap, PalettedContainer.Strategy strategy, PalettedContainer.Data<T> data, Operation<PalettedContainer<T>> original
    private PalettedContainer<T> addPresetValuesCopy(PalettedContainer<T> palettedContainer, Operation<PalettedContainer<T>> original) {
        var previous = Arguments.PRESET_VALUES.get();
        Arguments.PRESET_VALUES.set(antiXray$presetValues);
        try {
            return original.call(palettedContainer);
        } finally {
            Arguments.PRESET_VALUES.set(previous);
        }
    }

    @WrapOperation(
        method = "recreate",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/core/IdMap;Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;)Lnet/minecraft/world/level/chunk/PalettedContainer;"
        )
    )
    private PalettedContainer<T> addPresetValuesRecreate(IdMap<T> idMap, Object object, PalettedContainer.Strategy strategy, Operation<PalettedContainer<T>> original) {
        var previous = Arguments.PRESET_VALUES.get();
        Arguments.PRESET_VALUES.set(antiXray$presetValues);
        try {
            return original.call(idMap, strategy, strategy);
        } finally {
            Arguments.PRESET_VALUES.set(previous);
        }
    }

    @Unique
    private void antiXray$addPresetValues() {
        if (this.antiXray$presetValues != null && this.data.configuration().factory() != PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY) {
            for (T presetValue : this.antiXray$presetValues) {
                this.data.palette().idFor(presetValue);
            }
        }
    }
}
