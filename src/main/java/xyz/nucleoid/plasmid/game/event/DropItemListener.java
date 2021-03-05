package xyz.nucleoid.plasmid.game.event;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

/**
 * Called when a player attempts to drop an item, from the hotbar or from the inventory. Do note that the provided slot may be negative on
 * certain circumstances, so proceed with caution. In addition, this event will never fire if the THROW_ITEMS gamerule is set to deny.
 * The gamerule overrides the event checking, so the event is only fired if the game rule is not set, or is set to allow.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further handlers and drops the item.
 * <li>{@link ActionResult#FAIL} cancels further handlers and does not drop the item.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 */
public interface DropItemListener {
    EventType<DropItemListener> EVENT = EventType.create(DropItemListener.class, listeners -> (player, slot, stack) -> {
        for (DropItemListener listener : listeners) {
            ActionResult result = listener.onDrop(player, slot, stack);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onDrop(ServerPlayerEntity player, int slot, ItemStack stack);
}
