package me.drex.antixray.common.mixin;

import com.google.common.collect.Queues;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.Channel;
import me.drex.antixray.common.util.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Unique
    private final Queue<BooleanSupplier> antiXray$isActionReady = Queues.newConcurrentLinkedQueue();
    @Shadow
    private Channel channel;
    @Shadow
    @Final
    private Queue<Consumer<Connection>> pendingActions;

    @Shadow
    protected abstract void sendPacket(Packet<?> $$0, PacketSendListener $$1, boolean $$2);

    /**
     * @author Drex
     * @reason Wait for chunk packets to be ready (fully obfuscated)
     */
    @Overwrite
    private void flushQueue() {
        assert pendingActions.size() == antiXray$isActionReady.size();
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.pendingActions) {
                while (!this.antiXray$isActionReady.isEmpty() && (this.antiXray$isActionReady.peek().getAsBoolean())) {
                    pendingActions.poll().accept((Connection) (Object) this);
                    antiXray$isActionReady.poll();
                }
            }
        }
    }

    @Redirect(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V"
        )
    )
    public void redirectSendPacket(Connection instance, Packet<?> packet, PacketSendListener listener, boolean flush) {
        if (this.antiXray$isActionReady.isEmpty() && Util.isReady(packet)) {
            this.sendPacket(packet, listener, flush);
        } else {
            synchronized (this.pendingActions) {
                pendingActions.add(connection -> this.sendPacket(packet, listener, flush));
                antiXray$isActionReady.add(() -> Util.isReady(packet));
            }
        }
    }

    @WrapOperation(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"
        )
    )
    public <E> boolean addToActionReadyQueue(Queue<E> instance, E e, Operation<Boolean> original, Packet<?> packet) {
        synchronized (this.pendingActions) {
            antiXray$isActionReady.add(() -> Util.isReady(packet));
            return original.call(instance, e);
        }
    }

    @WrapOperation(
        method = {"flushChannel", "runOnceConnected"},
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"
        )
    )
    public <E> boolean addToActionReadyQueue(Queue<E> instance, E e, Operation<Boolean> original) {
        synchronized (this.pendingActions) {
            antiXray$isActionReady.add(() -> true);
            return original.call(instance, e);
        }
    }

}
