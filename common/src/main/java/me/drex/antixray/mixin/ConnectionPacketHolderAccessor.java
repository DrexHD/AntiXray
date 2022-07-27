package me.drex.antixray.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.PacketHolder.class)
public interface ConnectionPacketHolderAccessor {

    @Accessor("packet")
    Packet<?> getPacket();

    @Accessor("listener")
    PacketSendListener getListener();
}
