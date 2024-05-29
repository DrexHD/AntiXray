package me.drex.antixray.common.mixin;

import me.drex.antixray.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    public ServerLevel level;

    @Inject(
        method = "handleBlockBreakAction",
        at = @At("TAIL")
    )
    public void onPlayerBreakBlock(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        Util.getBlockController(this.level).onPlayerLeftClickBlock((ServerPlayerGameMode) (Object) this, blockPos, action, direction, i);
    }
}
