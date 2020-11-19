package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

/**
 * Called when a player attempts to drop an item, from the hotbar or from the inventory. Do note that the provided slot may be negative on
 * certain circumstances, so proceed with caution.
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

    ActionResult onDrop(PlayerEntity player, int slot, ItemStack stack);
}
