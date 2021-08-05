package me.drex.antixray.mixin;

import me.drex.antixray.util.LevelInterface;
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
public class ServerPlayerGameModeMixin {

    @Shadow protected ServerLevel level;

    @Inject(method = "handleBlockBreakAction", at = @At(value = "TAIL"))
    public void onBlockBreak(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, CallbackInfo ci) {
        ((LevelInterface)this.level).getChunkPacketBlockController().onPlayerLeftClickBlock((ServerPlayerGameMode) (Object)this, blockPos, action, direction, i);
    }

}
