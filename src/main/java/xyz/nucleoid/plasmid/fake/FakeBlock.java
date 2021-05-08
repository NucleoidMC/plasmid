package xyz.nucleoid.plasmid.fake;

import eu.pb4.polymer.block.VirtualBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

/**
 * @deprecated This interface is deprecated in favour of {@link eu.pb4.polymer.block.VirtualBlock}
 *
 * Represents a block that should be remapped to some vanilla "proxy" counterpart when being sent to clients
 */
@Deprecated
public interface FakeBlock extends VirtualBlock {
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

    static BlockState getProxy(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof FakeBlock) {
            return ((FakeBlock) block).asProxy(state);
        }

        return state;
    }

    static FluidState getProxy(FluidState state) {
        Block block = state.getBlockState().getBlock();
        if (block instanceof FakeBlock) {
            return ((FakeBlock) block).asProxy(state);
        }
        return state;
    }

    default Block getVirtualBlock() { return this.asProxy(); }

    default BlockState getVirtualBlockState(BlockState state) {
        return this.asProxy(state);
    }
}