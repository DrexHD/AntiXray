package me.drex.antixray.mixin;

import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.PalettedContainerDataInterface;
import me.drex.antixray.util.PalettedContainerInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: PalettedContainer changed a lot
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements PalettedContainerInterface<T> {
    private T[] presetValues;

    @Shadow private volatile PalettedContainer.Data data;

    @Override
    public void initValues(T[] presetValues, boolean initialize) {
        this.presetValues = presetValues;
        this.addPresetValues();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<T> chunkPacketInfo, int bottomBlockY) {
        ((PalettedContainerDataInterface<T>)this.data).write(friendlyByteBuf, chunkPacketInfo, presetValues, bottomBlockY);
    }

    @Inject(
            method = "onResize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/PalettedContainer$Data;copyFrom(Lnet/minecraft/world/level/chunk/Palette;Lnet/minecraft/util/BitStorage;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void addPresetValues(int i, T object, CallbackInfoReturnable<Integer> cir) {
        this.addPresetValues();
    }

    private void addPresetValues() {
        if (this.presetValues != null) {
            for (T presetValue : this.presetValues) {
                this.data.palette().idFor(presetValue);
            }
        }
    }
}
