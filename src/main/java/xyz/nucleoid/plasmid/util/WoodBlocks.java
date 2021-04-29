package xyz.nucleoid.plasmid.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class WoodBlocks {
    public static BlockState saplingOf(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.OAK_LEAVES) return Blocks.OAK_SAPLING.getDefaultState();
        if (block == Blocks.BIRCH_LEAVES) return Blocks.BIRCH_SAPLING.getDefaultState();
        if (block == Blocks.SPRUCE_LEAVES) return Blocks.SPRUCE_SAPLING.getDefaultState();
        if (block == Blocks.JUNGLE_LEAVES) return Blocks.JUNGLE_SAPLING.getDefaultState();
        if (block == Blocks.ACACIA_LEAVES) return Blocks.ACACIA_SAPLING.getDefaultState();
        if (block == Blocks.DARK_OAK_LEAVES) return Blocks.DARK_OAK_SAPLING.getDefaultState();

        return Blocks.AIR.getDefaultState();
    }

    public static BlockState planksOf(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.OAK_LOG) return Blocks.OAK_PLANKS.getDefaultState();
        if (block == Blocks.BIRCH_LOG) return Blocks.BIRCH_PLANKS.getDefaultState();
        if (block == Blocks.SPRUCE_LOG) return Blocks.SPRUCE_PLANKS.getDefaultState();
        if (block == Blocks.JUNGLE_LOG) return Blocks.JUNGLE_PLANKS.getDefaultState();
        if (block == Blocks.ACACIA_LOG) return Blocks.ACACIA_PLANKS.getDefaultState();
        if (block == Blocks.DARK_OAK_LOG) return Blocks.DARK_OAK_PLANKS.getDefaultState();

        return Blocks.AIR.getDefaultState();
    }
}