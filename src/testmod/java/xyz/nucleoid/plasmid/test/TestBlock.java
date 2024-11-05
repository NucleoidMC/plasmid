package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestBlock extends Block implements PolymerTexturedBlock {
    private final BlockState visualState;

    public TestBlock(Settings settings, BlockState state) {
        super(settings);
        this.visualState = state;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.visualState;
    }
}
