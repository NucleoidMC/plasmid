package xyz.nucleoid.plasmid.game.world.view;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

public final class HeightmapColumnSample implements BlockView {
    private static final BlockState VOID_BLOCK = Blocks.AIR.getDefaultState();

    private final int height;
    private final BlockState block;

    public HeightmapColumnSample(int height, BlockState block) {
        this.height = height;
        this.block = block;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.getY() <= this.height) {
            return this.block;
        }
        return VOID_BLOCK;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }
}
