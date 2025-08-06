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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    public ServerLevel level;
    @Shadow
    protected ServerPlayer player;

    @Inject(
            method = "handleBlockBreakAction",
            at = @At("TAIL")
    )
    public void onPlayerBreakBlock(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        if (this.player == null) return;

        var eyePos = this.player.getEyePosition(1.0F);
        var targetVec = blockPos.getCenter();

        ClipContext clipContext = new ClipContext(
                eyePos,
                targetVec,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                this.player
        );

        var rayResult = this.level.clip(clipContext);

        if (rayResult == null || !rayResult.getBlockPos().equals(blockPos)) {
            return;
        }
        Util.getBlockController(this.level).onPlayerLeftClickBlock((ServerPlayerGameMode)(Object)this, blockPos, action, direction, i);
    }

}
