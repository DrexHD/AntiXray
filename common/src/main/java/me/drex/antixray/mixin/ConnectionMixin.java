package me.drex.antixray.mixin;

import com.google.common.collect.Queues;
import io.netty.channel.Channel;
import me.drex.antixray.interfaces.IChunkPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow
    private Channel channel;


    @Shadow
    @Final
    private Queue<Consumer<Connection>> pendingActions;

    @Shadow
    protected abstract void sendPacket(Packet<?> $$0, PacketSendListener $$1, boolean $$2);

    @Unique
    private final Queue<BooleanSupplier> antiXray$isActionReady = Queues.newConcurrentLinkedQueue();

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
        if (isReady(packet)) {
            this.sendPacket(packet, listener, flush);
        } else {
            pendingActions.add(connection -> this.sendPacket(packet, listener, flush));
            antiXray$isActionReady.add(() -> isReady(packet));
        }
    }

    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"
        )
    )
    public void addToActionReadyQueue(Packet<?> packet, PacketSendListener listener, boolean flush, CallbackInfo ci) {
        antiXray$isActionReady.add(() -> isReady(packet));
    }

    @Inject(
        method = {"flushChannel", "runOnceConnected"},
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z"
        )
    )
    public void addToActionReadyQueue(CallbackInfo ci) {
        antiXray$isActionReady.add(() -> true);
    }

    private boolean isReady(Packet<?> packet) {
        if (packet instanceof ClientboundLevelChunkWithLightPacket combinedPacket) {
            return ((IChunkPacket) combinedPacket).isReady();
        } else {
            return true;
        }
    }

}
