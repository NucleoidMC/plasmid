package xyz.nucleoid.plasmid.fake;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Specialisation for {@link Fake} for ItemStack support
 *
 * @param <F> The item type being faked
 */
public interface FakeItem<F extends Item> extends Fake<F> {

    /**
     * The ItemStack to use
     *
     * @param itemStack The original stack
     * @return The stack to send to the client
     */
    ItemStack getFaking(ItemStack itemStack);
}
