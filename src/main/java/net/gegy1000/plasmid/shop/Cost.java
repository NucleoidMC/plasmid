package net.gegy1000.plasmid.shop;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class Cost {
    private Take take;
    private Text display;

    public static Cost ofIron(int iron) {
        return new Cost()
                .take(Items.IRON_INGOT, iron)
                .display(new LiteralText(iron + " iron").formatted(Formatting.GRAY));
    }

    public static Cost ofGold(int gold) {
        return new Cost()
                .take(Items.GOLD_INGOT, gold)
                .display(new LiteralText(gold + " gold").formatted(Formatting.GOLD));
    }

    public static Cost ofDiamonds(int diamonds) {
        return new Cost()
                .take(Items.DIAMOND, diamonds)
                .display(new LiteralText(diamonds + " diamonds").formatted(Formatting.AQUA));
    }

    public static Cost ofEmeralds(int emeralds) {
        return new Cost()
                .take(Items.EMERALD, emeralds)
                .display(new LiteralText(emeralds + " emeralds").formatted(Formatting.GREEN));
    }

    public static Cost no() {
        return new Cost().display(new LiteralText("Cannot buy"));
    }

    public Cost take(Take take) {
        this.take = take;
        return this;
    }

    public Cost take(Item item, int count) {
        this.take = (player, simulate) -> {
            int available = getAvailable(player, item);
            if (available >= count) {
                if (!simulate) {
                    take(player, item, count);
                    player.inventory.markDirty();
                }
                return true;
            }
            return false;
        };
        return this;
    }

    public Cost display(Text text) {
        this.display = text;
        return this;
    }

    public boolean tryTake(ServerPlayerEntity player, boolean simulate) {
        if (this.take == null) {
            return false;
        }
        return this.take.tryTake(player, simulate);
    }

    public Text getDisplay() {
        return this.display;
    }

    static int getAvailable(ServerPlayerEntity player, Item item) {
        int available = 0;
        for (int i = 0; i < player.inventory.size(); i++) {
            ItemStack stack = player.inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem().equals(item)) {
                available += stack.getCount();
            }
        }
        return available;
    }

    static void take(ServerPlayerEntity player, Item item, int count) {
        for (int slot = 0; slot < player.inventory.size(); slot++) {
            ItemStack stack = player.inventory.getStack(slot);

            if (!stack.isEmpty() && stack.getItem().equals(item)) {
                int remove = Math.min(count, stack.getCount());
                player.inventory.removeStack(slot, remove);

                count -= remove;
                if (count <= 0) {
                    return;
                }
            }
        }
    }

    public interface Take {
        boolean tryTake(ServerPlayerEntity player, boolean simulate);
    }
}
