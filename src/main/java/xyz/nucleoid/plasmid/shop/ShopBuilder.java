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
        var row = this.getOrCreateRow(this.currentRow);
        if (row.size() >= ShopUi.WIDTH) {
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
        var lines = this.rows;
        if (lines.size() <= index) {
            var line = new ArrayList<ShopEntry>();
            lines.add(line);
            return line;
        }
        return lines.get(index);
    }
}
