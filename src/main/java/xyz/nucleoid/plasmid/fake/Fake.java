package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Allows for Vanilla blocks/items to be faked for the client side
 *
 * @param <F> The type being faked to
 */
public interface Fake<F> {

    /**
     * The proxy to be sent to the client
     *
     * @return The proxy to be sent
     */
    F getFaking();

    /**
     * Resolves the proxy instance to be used to send the client
     *
     * @param entry Entry to send to the client
     * @param <F>   Type of entry. Used for convenience
     * @return      The proxied entry to use, or the entry itself
     */
    @SuppressWarnings("unchecked")
    static <F> F getProxy(F entry) {
        if (entry instanceof BlockState) {
            BlockState blockState = (BlockState) entry;
            Block block = blockState.getBlock();

            if (block instanceof FakeBlock) {
                return (F) ((FakeBlock<?>) block).getFaking(blockState);
            }
        }

        if (entry instanceof FluidState) {
            FluidState fluidState = (FluidState) entry;
            Block block = fluidState.getBlockState().getBlock();

            if (block instanceof FakeBlock) {
                return (F) ((FakeBlock<?>) block).getFaking(fluidState);
            }
        }

        if (entry instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) entry;
            Item item = itemStack.getItem();

            if (item instanceof FakeItem) {
                return (F) ((FakeItem<?>) item).getFaking(itemStack);
            }
        }

        if (entry instanceof Fake) {
            return ((Fake<? extends F>) entry).getFaking();
        }

        return entry;
    }
}
