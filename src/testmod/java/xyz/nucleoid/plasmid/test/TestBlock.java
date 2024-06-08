package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class TestBlock extends Block implements PolymerTexturedBlock {
    private final BlockState visualState;

    public TestBlock(Settings settings, BlockState state) {
        super(settings);
        this.visualState = state;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.visualState;
    }
}
