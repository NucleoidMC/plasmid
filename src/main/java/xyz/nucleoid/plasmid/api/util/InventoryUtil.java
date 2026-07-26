package xyz.nucleoid.plasmid.api.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class InventoryUtil {
    private static final List<CustomInventoryHandler> CUSTOM_INVENTORY_HANDLERS = new ArrayList<>();
    private InventoryUtil() {}

    public static void clear(ServerPlayer player) {
        player.getInventory().clearContent();
        player.inventoryMenu.getCraftSlots().clearContent();
        player.inventoryMenu.setCarried(ItemStack.EMPTY);
        player.containerMenu.setCarried(ItemStack.EMPTY);
        for (var handler : CUSTOM_INVENTORY_HANDLERS) {
            handler.clear(player);
        }
    }


    public static void addCustomHandler(CustomInventoryHandler handler) {
        CUSTOM_INVENTORY_HANDLERS.add(handler);
    }


    public interface CustomInventoryHandler {
        default void clear(ServerPlayer player) {};
    }
}
