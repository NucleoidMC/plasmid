package xyz.nucleoid.plasmid.shop;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ShopBuilder {
    final List<List<ShopEntry>> rows = new ArrayList<>();
    private int currentRow;

    public ShopBuilder addItem(ItemStack stack, Cost cost) {
        return this.add(ShopEntry.buyItem(stack).withCost(cost));
    }

    public ShopBuilder add(ShopEntry entry) {
        List<ShopEntry> row = this.getOrCreateRow(this.currentRow);
        if (row.size() >= ShopInventory.WIDTH) {
            row = this.getOrCreateRow(++this.currentRow);
        }

        row.add(entry);

        return this;
    }

    public ShopBuilder nextRow() {
        this.currentRow++;
        return this;
    }

    private List<ShopEntry> getOrCreateRow(int index) {
        List<List<ShopEntry>> lines = this.rows;
        if (lines.size() <= index) {
            List<ShopEntry> line = new ArrayList<>();
            lines.add(line);
            return line;
        }
        return lines.get(index);
    }
}
