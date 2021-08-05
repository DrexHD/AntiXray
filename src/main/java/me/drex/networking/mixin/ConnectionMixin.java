package me.drex.networking.mixin;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.drex.antixray.util.ClientboundLevelChunkPacketInterface;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    private Packet<?> cachedPacket = null;

    private boolean flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.queue) {
                while (!this.queue.isEmpty()) {
                    Connection.PacketHolder packetHolder = this.queue.peek(); // poll -> peek

                    if (packetHolder != null) { // Fix NPE (Spigot bug caused by handleDisconnection())
                        if (((ConnectionPacketHolderAccessor) packetHolder).getPacket() instanceof ClientboundLevelChunkPacket && !((ClientboundLevelChunkPacketInterface) ((ConnectionPacketHolderAccessor) packetHolder).getPacket()).isReady()) { // Check if the peeked packet is a chunk packet which is not ready
                            return false; // Return false if the peeked packet is a chunk packet which is not ready
                        } else {
                            this.queue.poll(); // poll here
                            this.sendPacket(((ConnectionPacketHolderAccessor) packetHolder).getPacket(), ((ConnectionPacketHolderAccessor) packetHolder).getListener()); // dispatch the packet
                        }
                    }

                }
            }
        }
        return true;
    }

    @Inject(method = "flushQueue", at = @At(value = "HEAD"), cancellable = true)
    public void replaceFlushQueue(CallbackInfo ci) {
        //Calling our own implementation
        this.flushQueue();
        ci.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "HEAD"))
    public void captureLocals(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericFutureListener, CallbackInfo ci) {
        cachedPacket = packet;
    }

    @Redirect(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;flushQueue()V"))
    public void noop(Connection connection) {
        //no-op
        //The call to our own flushQueue method has been moved inside the previous if statement (see below)
    }

    @Redirect(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;isConnected()Z"))
    public boolean redirectIfStatement(Connection connection) {
        return connection.isConnected() && this.flushQueue() && !(cachedPacket instanceof ClientboundLevelChunkPacket && !((ClientboundLevelChunkPacketInterface) cachedPacket).isReady());
    }
}
