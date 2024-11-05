package xyz.nucleoid.plasmid.util;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class InventoryUtil {
    private static final List<CustomInventoryHandler> CUSTOM_INVENTORY_HANDLERS = new ArrayList<>();
    private InventoryUtil() {}

    public static void clear(ServerPlayerEntity player) {
        player.getInventory().clear();
        player.playerScreenHandler.getCraftingInput().clear();
        player.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
        player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        for (var handler : CUSTOM_INVENTORY_HANDLERS) {
            handler.clear(player);
        }
    }


    public static void addCustomHandler(CustomInventoryHandler handler) {
        CUSTOM_INVENTORY_HANDLERS.add(handler);
    }


    public interface CustomInventoryHandler {
        default void clear(ServerPlayerEntity player) {};
    }
}
