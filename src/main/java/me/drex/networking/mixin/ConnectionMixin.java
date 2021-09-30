package me.drex.networking.mixin;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.drex.antixray.util.ClientboundLevelChunkPacketDataInterface;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.jetbrains.annotations.Nullable;
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
    private Queue<Connection.PacketHolder> queue;

    @Shadow
    protected abstract void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener);

    private boolean flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.queue) {
                while (!this.queue.isEmpty()) {
                    if (this.queue.peek() instanceof ConnectionPacketHolderAccessor packetAccessor) { // poll -> peek
                        if (!isReady(packetAccessor.getPacket())) {
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
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
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
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;isConnected()Z"
            )
    )
    public boolean redirectIfStatement(Connection connection, Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback) {
        return connection.isConnected() && this.flushQueue() && isReady(packet);
    }

    private boolean isReady(Packet<?> p) {
        if (p instanceof ClientboundLevelChunkWithLightPacket combinedPacket) {
            return ((ClientboundLevelChunkPacketDataInterface) combinedPacket.getChunkData()).isReady();
        } else {
            return true;
        }
    }

}
