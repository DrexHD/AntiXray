package me.drex.antixray.common.mixin;

import io.netty.channel.Channel;
import me.drex.antixray.common.util.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;


@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow
    private Channel channel;

    @Shadow
    @Final
    private Queue<Object> queue;

    @Shadow protected abstract void sendPacket(Packet<?> $$0, PacketSendListener $$1);

    private boolean flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.queue) {
                while (!this.queue.isEmpty()) {
                    if (this.queue.peek() instanceof ConnectionPacketHolderAccessor packetAccessor) { // poll -> peek
                        if (!Util.isReady(packetAccessor.getPacket())) {
                            return false; // Return false if the peeked packet is a chunk packet which is not ready
                        } else {
                            this.queue.poll(); // poll here
                            this.sendPacket(packetAccessor.getPacket(), packetAccessor.getListener()); // dispatch the packet
                        }
                    }

                }
            }
        }
        return true;
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;flushQueue()V"
        )
    )
    public void replaceFlushQueue(Connection connection) {
        // Calling our own implementation
        this.flushQueue();
    }

    @Redirect(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;flushQueue()V"
        )
    )
    public void noop(Connection connection) {
        // no-op
        // The call to our own flushQueue method has been moved inside the previous if statement (see below)
    }

    @Redirect(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;isConnected()Z"
        )
    )
    public boolean redirectIfStatement(Connection connection, Packet<?> packet, PacketSendListener listener) {
        return connection.isConnected() && this.flushQueue() && Util.isReady(packet);
    }

}