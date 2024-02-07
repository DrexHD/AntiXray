package me.drex.antixray.mixin.compat.imm_ptl_core;

import me.drex.antixray.interfaces.compat.imm_ptl_core.IPayload;
import me.drex.antixray.util.Util;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import qouteall.imm_ptl.core.network.PacketRedirection;

@Mixin(PacketRedirection.Payload.class)
public abstract class PacketRedirection$PayloadMixin implements IPayload {
    @Shadow
    @Final
    private Packet<? extends ClientCommonPacketListener> packet;

    @Override
    public boolean isReady() {
        return Util.isReady(packet);
    }
}
