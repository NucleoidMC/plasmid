package xyz.nucleoid.plasmid.fake;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;

@Deprecated
public final class Fake {
    /**
     * Resolves the proxy instance to be used to send the client
     *
     * @param entry Entry to send to the client
     * @param <T> Type of entry. Used for convenience
     * @return The proxied entry to use, or the entry itself
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(T entry) {
        if (entry instanceof FakeItem) {
            return (T) ((FakeItem) entry).asProxy();
        } else if (entry instanceof FakeBlock) {
            return (T) ((FakeBlock) entry).asProxy();
        } else if (entry instanceof BlockState) {
            return (T) FakeBlock.getProxy((BlockState) entry);
        } else if (entry instanceof FluidState) {
            return (T) FakeBlock.getProxy((FluidState) entry);
        } else if (entry instanceof ItemStack) {
            return (T) FakeItem.getProxy((ItemStack) entry);
        } else if (entry instanceof FakeEntityType) {
            return (T) ((FakeEntityType<?>) entry).asProxy();
        }

        return entry;
    }
}
