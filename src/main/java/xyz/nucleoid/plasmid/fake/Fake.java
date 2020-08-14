package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;

/**
 * Represents an object that should be remapped to some vanilla "proxy" counterpart when being sent to clients
 */
public interface Fake {
    /**
     * @return the proxy to be sent to the client
     */
    Object asProxy();

    /**
     * Resolves the proxy instance to be used to send the client
     *
     * @param entry Entry to send to the client
     * @param <T> Type of entry. Used for convenience
     * @return The proxied entry to use, or the entry itself
     */
    @SuppressWarnings("unchecked")
    static <T> T getProxy(T entry) {
        if (entry instanceof BlockState) {
            return (T) FakeBlock.getProxy((BlockState) entry);
        } else if (entry instanceof FluidState) {
            return (T) FakeBlock.getProxy((FluidState) entry);
        } else if (entry instanceof ItemStack) {
            return (T) FakeItem.getProxy((ItemStack) entry);
        }

        if (entry instanceof Fake) {
            Fake fake = (Fake) entry;
            return (T) fake.asProxy();
        }

        return entry;
    }
}
