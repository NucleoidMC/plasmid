package xyz.nucleoid.plasmid.test;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.ext.blocks.api.BlockModelType;
import eu.pb4.polymer.ext.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class TestBlock extends Block implements PolymerTexturedBlock {
    private final BlockState visualState;

    public TestBlock(Settings settings, BlockState state) {
        super(settings);
        this.visualState = state;
    }


    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.visualState.getBlock();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.visualState;
    }
}
