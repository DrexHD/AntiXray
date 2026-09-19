package me.drex.antixray.common.interfaces;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

public interface IChunkPacket extends IPacket {
    void antixray$setReady(boolean antixray$ready);

    static void antixray$setReady(ClientboundLevelChunkWithLightPacket chunkPacket, boolean ready) {
        ((IChunkPacket) (Object) chunkPacket).antixray$setReady(ready);
    }
}
