package me.drex.antixray.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.drex.antixray.common.util.Arguments;
import me.drex.antixray.common.util.ChunkPacketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
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
    public abstract int onResize(int i, T object);

    @Shadow
    protected abstract PalettedContainer.Data<T> createOrReuseData(PalettedContainer.Data<T> data, int i);

    @Shadow
    @Final
    private Strategy<T> strategy;

    @Unique
    private void antiXray$initializePresetValues() {
        if (Arguments.PRESET_VALUES.isBound()) {
            //noinspection unchecked
            this.antiXray$presetValues = (T[]) Arguments.PRESET_VALUES.get();
        }
    }

    @WrapOperation(
        method = "unpack",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/chunk/Strategy;Lnet/minecraft/world/level/chunk/Configuration;Lnet/minecraft/util/BitStorage;Lnet/minecraft/world/level/chunk/Palette;)Lnet/minecraft/world/level/chunk/PalettedContainer;"
        )
    )
    private static <T> PalettedContainer<T> addPaletteEntryListArgument(
        Strategy<T> strategy, Configuration configuration, BitStorage bitStorage, Palette<T> palette,
        Operation<PalettedContainer<T>> original, @Local(name = "paletteEntries") List<T> paletteEntries
    ) {
        return ScopedValue.where(Arguments.PALETTE_ENTRIES, paletteEntries).call(() -> original.call(strategy, configuration, bitStorage, palette));
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/chunk/Strategy;Lnet/minecraft/world/level/chunk/Configuration;Lnet/minecraft/util/BitStorage;Lnet/minecraft/world/level/chunk/Palette;)V",
        at = @At("TAIL")
    )
    private void addPresetValuesWithEntries(Strategy<T> strategy, Configuration configuration, BitStorage bitStorage, Palette<T> palette, CallbackInfo ci) {
        antiXray$initializePresetValues();
        
        if (!Arguments.PALETTE_ENTRIES.isBound()) return;
        //noinspection unchecked
        List<T> paletteEntries = (List<T>) Arguments.PALETTE_ENTRIES.get();

        if (antiXray$presetValues != null
            && paletteEntries != null
            && (configuration instanceof net.minecraft.world.level.chunk.Configuration.Simple simpleFactory && simpleFactory.factory() == Strategy.SINGLE_VALUE_PALETTE_FACTORY
            ? this.data.palette().valueFor(0) != Blocks.AIR.defaultBlockState()
            : !(configuration instanceof net.minecraft.world.level.chunk.Configuration.Global))) {
            // In 1.18 Mojang unfortunately removed code that already handled possible resize operations on read from disk for us
            // We readd this here but in a smarter way than it was before
            int maxSize = 1 << configuration.bitsInMemory();

            for (T presetValue : antiXray$presetValues) {
                if (this.data.palette().getSize() >= maxSize) {
                    java.util.Set<T> allValues = new java.util.HashSet<>(paletteEntries);
                    allValues.addAll(Arrays.asList(antiXray$presetValues));
                    int newBits = Mth.ceillog2(allValues.size());

                    if (newBits > configuration.bitsInMemory()) {
                        this.onResize(newBits, null);
                    }

                    break;
                }

                this.data.palette().idFor(presetValue, (PalettedContainer) (Object) this);
            }
        }
    }

    @Inject(
        method = "<init>(Ljava/lang/Object;Lnet/minecraft/world/level/chunk/Strategy;)V",
        at = @At("TAIL")
    )
    public void addPresetValuesInit(Object object, Strategy<T> strategy, CallbackInfo ci) {
        antiXray$initializePresetValues();
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/chunk/PalettedContainer;)V",
        at = @At("TAIL")
    )
    public void addPresetValuesInit(PalettedContainer<T> palettedContainer, CallbackInfo ci) {
        antiXray$initializePresetValues();
    }

    @Redirect(
        method = "onResize",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/PalettedContainer;createOrReuseData(Lnet/minecraft/world/level/chunk/PalettedContainer$Data;I)Lnet/minecraft/world/level/chunk/PalettedContainer$Data;"
        )
    )
    private PalettedContainer.Data<T> addPresetValues(PalettedContainer<T> container, PalettedContainer.Data<T> data, int bits, int i, T objectAdded) {
        if (this.antiXray$presetValues != null && objectAdded != null && data.configuration() instanceof Configuration.Simple simpleFactory && simpleFactory.factory() == Strategy.SINGLE_VALUE_PALETTE_FACTORY) {
            int duplicates = 0;
            List<T> presetValues = Arrays.asList(this.antiXray$presetValues);
            duplicates += presetValues.contains(objectAdded) ? 1 : 0;
            duplicates += presetValues.contains(data.palette().valueFor(0)) ? 1 : 0;
            final int size = 1 << this.strategy.getConfigurationForBitCount(bits).bitsInMemory();
            bits = Mth.ceillog2(size + presetValues.size() - duplicates);
        }

        return this.createOrReuseData(data, bits);
    }

    @WrapOperation(
        method = "onResize",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/Palette;idFor(Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PaletteResize;)I"
        )
    )
    private int addPresetValues(Palette<T> palette, T object, PaletteResize<T> paletteResize, Operation<Integer> original) {
        this.antiXray$addPresetValues();
        return object == null ? -1 : original.call(palette, object, paletteResize);
    }

    @Inject(
        method = "write",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/PalettedContainer$Data;write(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/core/IdMap;)V",
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
    private PalettedContainer<T> addPresetValuesCopy(PalettedContainer<T> palettedContainer, Operation<PalettedContainer<T>> original) {
        return ScopedValue.where(Arguments.PRESET_VALUES, antiXray$presetValues).call(() -> original.call(palettedContainer));
    }

    @WrapOperation(
        method = "recreate",
        at = @At(
            value = "NEW",
            target = "(Ljava/lang/Object;Lnet/minecraft/world/level/chunk/Strategy;)Lnet/minecraft/world/level/chunk/PalettedContainer;"
        )
    )
    private PalettedContainer<T> addPresetValuesRecreate(Object object, Strategy<T> strategy, Operation<PalettedContainer<T>> original) {
        return ScopedValue.where(Arguments.PRESET_VALUES,  antiXray$presetValues).call(() -> original.call(object, strategy));
    }

    @Unique
    private void antiXray$addPresetValues() {
        if (this.antiXray$presetValues != null && !(this.data.configuration() instanceof Configuration.Global)) {
            for (T presetValue : this.antiXray$presetValues) {
                this.data.palette().idFor(presetValue, (PalettedContainer) (Object) this);
            }
        }
    }
}
