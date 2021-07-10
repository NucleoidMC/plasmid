package xyz.nucleoid.plasmid.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class ShopInventory implements Inventory {
    static final int WIDTH = 9;
    static final int HEIGHT = 6;

    static final int PADDING = 1;
    static final int PADDED_WIDTH = WIDTH - PADDING * 2;

    static final int SIZE = WIDTH * HEIGHT;

    private final ShopEntry[] elements = new ShopEntry[this.size()];

    private final ServerPlayerEntity player;
    private final Consumer<ShopBuilder> builder;

    ShopInventory(ServerPlayerEntity player, Consumer<ShopBuilder> builder) {
        this.player = player;
        this.builder = builder;
        this.buildGrid();
    }

    private void buildGrid() {
        ShopBuilder builder = new ShopBuilder();
        this.builder.accept(builder);

        this.fillGrid(builder.rows);
    }

    private void fillGrid(List<List<ShopEntry>> rows) {
        Arrays.fill(this.elements, null);

        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            List<ShopEntry> row = rows.get(rowIdx);
            int minColumn = (WIDTH - row.size()) / 2;
            for (int column = 0; column < row.size(); column++) {
                ShopEntry element = row.get(column);
                this.elements[(column + minColumn) + rowIdx * WIDTH] = element;
            }
        }
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public ItemStack getStack(int index) {
        ShopEntry element = this.elements[index];
        if (element == null) {
            return ItemStack.EMPTY;
        }
        return element.createIcon(this.player);
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int index) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    private void handleElementClick(int index) {
        ShopEntry element = this.elements[index];
        if (element != null) {
            element.onClick(this.player);
        }

        this.buildGrid();

        var screenHandler = this.player.currentScreenHandler;
        screenHandler.setCursorStack(ItemStack.EMPTY);
        screenHandler.syncState();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
    }
}
