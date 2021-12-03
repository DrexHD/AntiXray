package me.drex.antixray.mixin;

import me.drex.antixray.interfaces.IPalettedContainer;
import me.drex.antixray.util.ChunkPacketInfo;
import net.minecraft.core.IdMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements IPalettedContainer<T> {
    @Unique
    private T[] presetValues;

    @Unique
    private List<T> paletteEntries;

    @Shadow
    private volatile PalettedContainer.Data<T> data;
    @Shadow
    @Final
    private PalettedContainer.Strategy strategy;
    @Shadow
    @Final
    private IdMap<T> registry;

    @Shadow
    public abstract void acquire();

    @Shadow
    public abstract void release();

    @Shadow
    public abstract int onResize(int i, T object);

    @Shadow
    protected abstract PalettedContainer.Data<T> createOrReuseData(PalettedContainer.@Nullable Data<T> data, int i);

    @Inject(
            method = "<init>(Lnet/minecraft/core/IdMap;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;Lnet/minecraft/world/level/chunk/PalettedContainer$Configuration;Lnet/minecraft/util/BitStorage;Ljava/util/List;)V",
            at = @At("RETURN")
    )
    private void prepareVariables(IdMap<T> idMap, PalettedContainer.Strategy strategy, PalettedContainer.Configuration<T> configuration, BitStorage bitStorage, List<T> list, CallbackInfo ci) {
        this.paletteEntries = list;
    }

    @Redirect(
            method = "onResize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/PalettedContainer;createOrReuseData(Lnet/minecraft/world/level/chunk/PalettedContainer$Data;I)Lnet/minecraft/world/level/chunk/PalettedContainer$Data;"
            )
    )
    private PalettedContainer.Data<T> addPresetValues(PalettedContainer<T> container, PalettedContainer.Data<T> data, int bits, int i, T object) {
        if (this.presetValues != null && object != null && data.configuration().factory() == PalettedContainer.Strategy.SINGLE_VALUE_PALETTE_FACTORY) {
            int duplicates = 0;
            List<T> presetValues = Arrays.asList(this.presetValues);
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
        this.addPresetValues();
        return object == null ? -1 : palette.idFor(object);
    }

    /**
     * Adds preset values after initialization of this PalettedContainer.
     * Only use this method after {@link PalettedContainer(IdMap, PalettedContainer.Strategy, PalettedContainer.Configuration, BitStorage, List)}.
     * For now, this only used in {@link ChunkSerializer#read(ServerLevel, PoiManager, ChunkPos, CompoundTag)}
     * if the palette gets read from NBT.
     */
    @Override
    public void addPresetValuesWithEntries(T[] presetValues) {
        this.presetValues = presetValues;
        PalettedContainer.Configuration<T> provider = this.data.configuration();
        if (presetValues != null && (provider.factory() == PalettedContainer.Strategy.SINGLE_VALUE_PALETTE_FACTORY ? this.data.palette().valueFor(0) != Blocks.AIR.defaultBlockState() : provider.factory() != PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY)) {
            // In 1.18 Mojang unfortunately removed code that already handled possible resize operations on read from disk for us
            // We read this here but in a smarter way than it was before
            int maxSize = 1 << provider.bits();

            for (T presetValue : presetValues) {
                if (this.data.palette().getSize() >= maxSize) {
                    Set<T> allValues = new HashSet<>(this.paletteEntries);
                    allValues.addAll(Arrays.asList(presetValues));
                    int newBits = Mth.ceillog2(allValues.size());

                    if (newBits > provider.bits()) {
                        this.onResize(newBits, null);
                    }

                    break;
                }

                this.data.palette().idFor(presetValue);
            }
        }
    }

    /**
     * Adds preset values after initialization of this PalettedContainer.
     * Use this method after {@link PalettedContainer(IdMap, Object, PalettedContainer.Strategy)}.
     * or {@link PalettedContainer(IdMap, PalettedContainer.Strategy, PalettedContainer.Data)})}.
     */
    @Override
    public void addPresetValues(T[] presetValues) {
        this.presetValues = presetValues;
    }

    @Override
    public void write(FriendlyByteBuf buf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY) {
        this.acquire();
        try {
            buf.writeByte(this.data.storage().getBits());
            this.data.palette().write(buf);
            if (chunkPacketInfo != null) {
                // Bottom block to 0 based chunk section index
                int chunkSectionIndex = (bottomBlockY >> 4) - chunkPacketInfo.getChunk().getMinSection();
                chunkPacketInfo.setBits(chunkSectionIndex, this.data.configuration().bits());
                chunkPacketInfo.setPalette(chunkSectionIndex, this.data.palette());
                chunkPacketInfo.setIndex(chunkSectionIndex, buf.writerIndex() + FriendlyByteBuf.getVarIntSize(this.data.storage().getRaw().length));
            }

            buf.writeLongArray(this.data.storage().getRaw());
            if (chunkPacketInfo != null) {
                int chunkSectionIndex = (bottomBlockY >> 4) - chunkPacketInfo.getChunk().getMinSection();
                chunkPacketInfo.setPresetValues(chunkSectionIndex, this.presetValues);
            }
        } finally {
            this.release();
        }
    }

    private void addPresetValues() {
        if (this.presetValues != null && this.data.configuration().factory() != PalettedContainer.Strategy.GLOBAL_PALETTE_FACTORY) {
            for (T presetValue : this.presetValues) {
                this.data.palette().idFor(presetValue);
            }
        }
    }
}
