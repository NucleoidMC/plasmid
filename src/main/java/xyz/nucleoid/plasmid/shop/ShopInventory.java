package xyz.nucleoid.plasmid.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.function.Consumer;

public final class ShopInventory implements Inventory {
    private static final int WIDTH = 9;
    private static final int PADDING = 1;
    private static final int PADDED_WIDTH = WIDTH - PADDING * 2;

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

        this.buildGrid(builder.elements.toArray(new ShopEntry[0]));
    }

    private void buildGrid(ShopEntry[] elements) {
        Arrays.fill(this.elements, null);

        int rows = MathHelper.ceil((double) elements.length / PADDED_WIDTH);
        for (int row = 0; row < rows; row++) {
            ShopEntry[] resolved = this.resolveRow(elements, row);
            int minColumn = (WIDTH - resolved.length) / 2;
            for (int column = 0; column < resolved.length; column++) {
                ShopEntry element = resolved[column];
                this.elements[(column + minColumn) + row * WIDTH] = element;
            }
        }
    }

    private ShopEntry[] resolveRow(ShopEntry[] elements, int row) {
        int minId = Integer.MAX_VALUE;
        int maxId = Integer.MIN_VALUE;
        int rowStart = row * PADDED_WIDTH;
        int rowEnd = Math.min(rowStart + PADDED_WIDTH, elements.length);
        for (int idx = rowStart; idx < rowEnd; idx++) {
            if (elements[idx] != null) {
                if (idx < minId) {
                    minId = idx;
                }
                if (idx > maxId) {
                    maxId = idx;
                }
            }
        }
        ShopEntry[] resolved = new ShopEntry[(maxId - minId) + 1];
        System.arraycopy(elements, minId, resolved, 0, resolved.length);
        return resolved;
    }

    @Override
    public int size() {
        return WIDTH * 6;
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
        this.player.inventory.setCursorStack(ItemStack.EMPTY);
        this.player.updateCursorStack();

        ShopEntry element = this.elements[index];
        if (element != null) {
            element.onClick(this.player);
        }

        this.buildGrid();
        this.player.onHandlerRegistered(this.player.currentScreenHandler, this.player.currentScreenHandler.getStacks());
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
