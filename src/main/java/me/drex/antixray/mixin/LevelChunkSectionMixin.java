package me.drex.antixray.mixin;

import me.drex.antixray.mixin.accessor.ChunkAccessAccessor;
import me.drex.antixray.util.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
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
    @Shadow
    private short nonEmptyBlockCount;
    @Shadow
    @Final
    private int bottomBlockY;

    @Shadow
    @Final
    private PalettedContainer<Biome> biomes;

    @Override
    @SuppressWarnings("unchecked")
    public void addBlockPresets(final Level level) {
        // Add preset block states
        BlockState[] presetBlockStates = null;
        if (level instanceof LevelInterface levelInterface) {
            final ChunkPacketBlockController controller = levelInterface.getChunkPacketBlockController();
            if (controller != null) {
                presetBlockStates = controller.getPresetBlockStates(level, (LevelChunkSection) (Object) this);
            }
        }
        ((PalettedContainerInterface<BlockState>) this.states).initValues(presetBlockStates);
    }

    @Override
    public void initValues(final LevelHeightAccessor chunk) {
        if (chunk instanceof ServerLevel level) {
            addBlockPresets(level);
        } else if (chunk instanceof LevelChunk levelChunk) {
            addBlockPresets(levelChunk.getLevel());
        } else if (chunk instanceof ChunkAccessAccessor accessor) {
            LevelHeightAccessor heightAccessor = accessor.getLevelHeightAccessor();
            if (heightAccessor instanceof Level level) {
                addBlockPresets(level);
            } else {
                initValues(heightAccessor);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(final FriendlyByteBuf friendlyByteBuf, final ChunkPacketInfo<BlockState> chunkPacketInfo) {
        friendlyByteBuf.writeShort(this.nonEmptyBlockCount);
        ((PalettedContainerInterface<BlockState>) this.states).write(friendlyByteBuf, chunkPacketInfo, this.bottomBlockY);
        this.biomes.write(friendlyByteBuf);
    }
}
