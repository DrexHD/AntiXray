package me.drex.antixray.mixin;

import me.drex.antixray.util.ChunkPacketInfo;
import me.drex.antixray.util.LevelChunkSectionInterface;
import me.drex.antixray.util.LevelInterface;
import me.drex.antixray.util.PalettedContainerInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements LevelChunkSectionInterface {

    @Mutable
    @Shadow
    @Final
    private PalettedContainer<BlockState> states;
    @Shadow private short nonEmptyBlockCount;
    @Shadow @Final private int bottomBlockY;

    @Override @SuppressWarnings("unchecked")
    public void initValues(Level level, boolean initializeBlocks) {
         // Add preset block states
        ((PalettedContainerInterface<BlockState>) this.states).initValues(level == null ? null : ((LevelInterface) level).getChunkPacketBlockController().getPresetBlockStates(level, (LevelChunkSection) (Object) this), initializeBlocks);
    }

    @Override @SuppressWarnings("unchecked")
    public void write(FriendlyByteBuf friendlyByteBuf, ChunkPacketInfo<BlockState> chunkPacketInfo) {
        friendlyByteBuf.writeShort(this.nonEmptyBlockCount);
        ((PalettedContainerInterface<BlockState>)this.states).write(friendlyByteBuf, chunkPacketInfo, this.bottomBlockY);
    }
}
