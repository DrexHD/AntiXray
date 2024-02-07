package me.drex.antixray.mixin.compat.imm_ptl_core;

import me.drex.antixray.interfaces.IPacket;
import me.drex.antixray.interfaces.compat.imm_ptl_core.IPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketMixin implements IPacket {
    @Shadow
    @Final
    private CustomPacketPayload payload;

    @Override
    public boolean isReady() {
        if (payload instanceof IPayload redirectionPayload) {
            return redirectionPayload.isReady();
        }
        return true;
    }
}
