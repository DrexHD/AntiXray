package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelChunkSectionInterface;
import me.drex.antixray.util.ProtoChunkInterface;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin implements ProtoChunkInterface {

    @Shadow @Final private LevelChunkSection[] sections;
    private Level level;

    @Inject(method = "getOrCreateSection", at = @At(value = "RETURN"))
    public void initValues(int i, CallbackInfoReturnable<LevelChunkSection> cir) {
        ((LevelChunkSectionInterface)this.sections[i]).initValues(level, true);
    }

    @Override
    public void initValues(Level level) {
        this.level = level;
    }
}
