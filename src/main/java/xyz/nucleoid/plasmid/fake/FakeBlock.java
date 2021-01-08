package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

/**
 * Represents a block that should be remapped to some vanilla "proxy" counterpart when being sent to clients
 */
public interface FakeBlock {
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

    /**
     * Resolves the proxy {@link BlockState} instance to be sent to the client. If the associated {@link Block} of this
     * {@link BlockState} is an instance of {@link FakeBlock}, the {@link #asProxy(BlockState)} method will be used
     *
     * @param state The server side {@link BlockState}
     * @return The {@link BlockState} to send to the client
     */
    static BlockState getProxy(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof FakeBlock) {
            return ((FakeBlock) block).asProxy(state);
        }

        return state;
    }

    /**
     * Resolves the proxy {@link FluidState} instance to be sent to the client. If the associated {@link Block} of this
     * {@link FluidState} is an instance of {@link FakeBlock}, the {@link #asProxy(FluidState)} method will be used
     *
     * @param state The server side {@link FluidState}
     * @return The {@link FluidState} to send to the client
     */
    static FluidState getProxy(FluidState state) {
        Block block = state.getBlockState().getBlock();
        if (block instanceof FakeBlock) {
            return ((FakeBlock) block).asProxy(state);
        }

        return state;
    }
}
