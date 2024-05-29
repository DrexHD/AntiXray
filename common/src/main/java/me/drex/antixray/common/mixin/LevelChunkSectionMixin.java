package me.drex.antixray.common.mixin;

import me.drex.antixray.common.interfaces.IChunkSection;
import me.drex.antixray.common.interfaces.ILevel;
import me.drex.antixray.common.interfaces.IPalettedContainer;
import me.drex.antixray.common.util.ChunkPacketInfo;
import me.drex.antixray.common.util.controller.ChunkPacketBlockController;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements IChunkSection {

    @Unique
    private int bottomBlockY;

    @Shadow
    @Final
    private PalettedContainer<BlockState> states;

    @Shadow
    private PalettedContainerRO<Holder<Biome>> biomes;

    @Shadow
    private short nonEmptyBlockCount;

    @Override
    public void init(int bottomBlockY) {
        this.bottomBlockY = bottomBlockY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addBlockPresets(Level level) {
        // Add preset block states
        BlockState[] presetBlockStates = null;
        if (level instanceof ILevel levelInterface) {
            final ChunkPacketBlockController controller = levelInterface.getChunkPacketBlockController();
            if (controller != null) {
                presetBlockStates = controller.getPresetBlockStates(level, this.bottomBlockY);
            }
        }
        ((IPalettedContainer<BlockState>) this.states).addPresetValues(presetBlockStates);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(FriendlyByteBuf byteBuf, ChunkPacketInfo<BlockState> packetInfo) {
        byteBuf.writeShort(this.nonEmptyBlockCount);
        ((IPalettedContainer<BlockState>) this.states).write(byteBuf, packetInfo, this.bottomBlockY);
        this.biomes.write(byteBuf);
    }
}
