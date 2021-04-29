package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public final class WoodBlocks {
    public static Block saplingOf(Block block) {
        if(block == Blocks.OAK_LEAVES) { return Blocks.OAK_SAPLING; }
        else if(block == Blocks.BIRCH_LEAVES) { return Blocks.BIRCH_SAPLING; }
        else if(block == Blocks.SPRUCE_LEAVES) { return Blocks.SPRUCE_SAPLING; }
        else if(block == Blocks.JUNGLE_LEAVES) { return Blocks.JUNGLE_SAPLING; }
        else if(block == Blocks.ACACIA_LEAVES) { return Blocks.ACACIA_SAPLING; }
        else if(block == Blocks.DARK_OAK_LEAVES) { return Blocks.DARK_OAK_SAPLING; }
        else { return null; }
    }

    public static Block planksOf(Block block) {
        if(block == Blocks.OAK_LOG) { return Blocks.OAK_PLANKS; }
        else if(block == Blocks.BIRCH_LOG) { return Blocks.BIRCH_PLANKS; }
        else if(block == Blocks.SPRUCE_LOG) { return Blocks.SPRUCE_PLANKS; }
        else if(block == Blocks.JUNGLE_LOG) { return Blocks.JUNGLE_PLANKS; }
        else if(block == Blocks.ACACIA_LOG) { return Blocks.ACACIA_PLANKS; }
        else if(block == Blocks.DARK_OAK_LOG) { return Blocks.DARK_OAK_PLANKS; }
        else { return null; }
    }
}