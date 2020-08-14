package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

/**
 * Specialisation for {@link Fake} for blocks
 */
public interface FakeBlock extends Fake {
    @Override
    Block asProxy();

    /**
     * Maps a {@link BlockState} to its proxy counterpart to be sent to the client
     *
     * @param state The original block state
     * @return The block state to send to the client
     */
    BlockState asProxy(BlockState state);

    /**
     * Maps a {@link FluidState} to its proxy counterpart to be sent to the client
     *
     * @param state The fluid block state
     * @return The fluid state to send to the client
     */
    default FluidState asProxy(FluidState state) {
        return this.asProxy(state.getBlockState()).getFluidState();
    }
}
