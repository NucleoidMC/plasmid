package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

/**
 * Specialisation for {@link Fake} for BlockState support
 *
 * @param <F> The block type being faked
 */
public interface FakeBlock<F extends Block> extends Fake<F> {

    /**
     * The block state to use
     *
     * @param state The original block state
     * @return The block state to send to the client
     */
    BlockState getFaking(BlockState state);
}
