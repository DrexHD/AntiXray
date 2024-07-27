package me.drex.antixray.common.mixin;

import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.network.Connection$PacketHolder")
public interface ConnectionPacketHolderAccessor {
    @Accessor("packet")
    Packet<?> getPacket();

    @Accessor("listener")
    PacketSendListener getListener();
}