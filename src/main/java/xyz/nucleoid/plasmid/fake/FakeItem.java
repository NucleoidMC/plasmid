package xyz.nucleoid.plasmid.fake;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Specialisation for {@link Fake} for items
 */
public interface FakeItem extends Fake {
    @Override
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

    static ItemStack getProxy(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof FakeItem) {
            return ((FakeItem) item).asProxy(stack);
        }

        return stack;
    }
}
