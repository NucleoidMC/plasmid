package xyz.nucleoid.plasmid.fake;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Represents an item that should be remapped to some vanilla "proxy" counterpart when being sent to clients
 */
public interface FakeItem {
    Item asProxy();

    /**
     * Maps an {@link ItemStack} to its proxy counterpart to be sent to the client
     *
     * @param stack The original stack
     * @return The stack to send to the client
     */
    default ItemStack asProxy(ItemStack stack) {
        return stack;
    }

    /**
     * Resolves the proxy {@link ItemStack} instance to be sent to the client. If the associated {@link Item} of this
     * {@link ItemStack} is an instance of {@link FakeItem}, the {@link #asProxy(ItemStack)} method will be used
     *
     * <p>This method is intended for use in case the item has information which the client would not be able to
     * normally see, so that lore could be appended, or its visuals changed
     *
     * @param stack The server side {@link ItemStack}
     * @return The {@link ItemStack} to send to the client
     */
    static ItemStack getProxy(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof FakeItem) {
            return ((FakeItem) item).asProxy(stack);
        }

        return stack;
    }
}
