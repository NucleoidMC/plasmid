package xyz.nucleoid.plasmid.fake;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Specialisation for {@link Fake} for stack support
 *
 * @param <F> The item type being faked
 */
public interface FakeItem<F extends Item> extends Fake<F> {

    /**
     * The stack to use
     *
     * @param stack The original stack
     * @return The stack to send to the client
     */
    default ItemStack getFaking(ItemStack stack) {
        ItemStack fakingStack = new ItemStack(this.getFaking(), stack.getCount());

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            fakingStack.setTag(tag.copy());
        }

        return fakingStack;
    }
}
