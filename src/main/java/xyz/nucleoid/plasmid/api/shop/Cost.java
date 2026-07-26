package xyz.nucleoid.plasmid.api.shop;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class Cost {
    private Take take;
    private Component display;

    public static Cost ofIron(int iron) {
        return new Cost()
                .take(Items.IRON_INGOT, iron)
                .display(Component.translatable("text.plasmid.shop.cost.iron", iron).withStyle(ChatFormatting.GRAY));
    }

    public static Cost ofGold(int gold) {
        return new Cost()
                .take(Items.GOLD_INGOT, gold)
                .display(Component.translatable("text.plasmid.shop.cost.gold", gold).withStyle(ChatFormatting.GOLD));
    }

    public static Cost ofDiamonds(int diamonds) {
        return new Cost()
                .take(Items.DIAMOND, diamonds)
                .display(Component.translatable("text.plasmid.shop.cost.diamonds", diamonds).withStyle(ChatFormatting.AQUA));
    }

    public static Cost ofEmeralds(int emeralds) {
        return new Cost()
                .take(Items.EMERALD, emeralds)
                .display(Component.translatable("text.plasmid.shop.cost.emeralds", emeralds).withStyle(ChatFormatting.GREEN));
    }

    public static Cost free() {
        return new Cost()
                .take((player, simulate) -> true)
                .display(Component.translatable("text.plasmid.shop.cost.free"));
    }

    public static Cost ofItem(Item item, int count, Component text) {
        return new Cost()
                .take(item, count)
                .display(text);
    }

    public static Cost ofItem(Item item, int count) {
        return new Cost()
                .take(item, count)
                .display(Component.translatable("text.plasmid.shop.cost.custom", count, item.getName(item.getDefaultInstance())));
    }

    public static Cost no() {
        return new Cost().display(Component.translatable("text.plasmid.shop.cost.no"));
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
                    player.getInventory().setChanged();
                }
                return true;
            }
            return false;
        };
        return this;
    }

    public Cost display(Component text) {
        this.display = text;
        return this;
    }

    public boolean tryTake(ServerPlayer player, boolean simulate) {
        if (this.take == null) {
            return false;
        }
        return this.take.tryTake(player, simulate);
    }

    public Component getDisplay() {
        return this.display;
    }

    static int getAvailable(ServerPlayer player, Item item) {
        int available = 0;
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem().equals(item)) {
                available += stack.getCount();
            }
        }
        return available;
    }

    static void take(ServerPlayer player, Item item, int count) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);

            if (!stack.isEmpty() && stack.getItem().equals(item)) {
                int remove = Math.min(count, stack.getCount());
                inventory.removeItem(slot, remove);

                count -= remove;
                if (count <= 0) {
                    return;
                }
            }
        }
    }

    public boolean canBuy(ServerPlayer player) {
        return this.tryTake(player, true);
    }

    public boolean takeItems(ServerPlayer player) {
        return this.tryTake(player, false);

    }

    public interface Take {
        boolean tryTake(ServerPlayer player, boolean simulate);
    }
}
