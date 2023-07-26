package xyz.nucleoid.plasmid.util;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InventoryUtil {
    private InventoryUtil() {}

    public static void clear(ServerPlayerEntity player) {
        player.getInventory().clear();
        player.playerScreenHandler.clearCraftingSlots();
        player.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
        player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
    }
}
