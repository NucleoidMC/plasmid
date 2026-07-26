package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class TestBlock extends Block implements PolymerTexturedBlock {
    private final BlockState visualState;

    public TestBlock(Properties settings, BlockState state) {
        super(settings);
        this.visualState = state;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return this.visualState;
    }
}
