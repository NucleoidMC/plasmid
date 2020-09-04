package xyz.nucleoid.plasmid.fake;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

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
        ItemStack proxy = new ItemStack(this.asProxy(), stack.getCount());

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            proxy.setTag(tag.copy());
        }

        proxy.setCustomName(((Item) this).getName(stack));

        return proxy;
    }

    static ItemStack getProxy(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof FakeItem) {
            return ((FakeItem) item).asProxy(stack);
        }

        return stack;
    }
}
