package net.gegy1000.plasmid.shop;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ShopBuilder {
    final List<ShopEntry> elements = new ArrayList<>();

    public ShopBuilder addItem(ItemStack stack, Cost cost) {
        return this.add(ShopEntry.buyItem(stack).withCost(cost));
    }

    public ShopBuilder add(ShopEntry entry) {
        this.elements.add(entry);
        return this;
    }
}
