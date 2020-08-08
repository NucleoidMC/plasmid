package net.gegy1000.plasmid.game.world.view;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

public final class VoidBlockView implements BlockView {
    public static final VoidBlockView INSTANCE = new VoidBlockView();

    private static final BlockState VOID_BLOCK = Blocks.AIR.getDefaultState();
    private static final FluidState VOID_FLUID = Fluids.EMPTY.getDefaultState();

    private VoidBlockView() {
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return VOID_BLOCK;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return VOID_FLUID;
    }
}
