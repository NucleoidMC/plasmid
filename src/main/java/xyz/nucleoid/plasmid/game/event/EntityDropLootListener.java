package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

import java.util.List;

/**
 * Called when any {@link net.minecraft.entity.LivingEntity} drops loot in a {@link xyz.nucleoid.plasmid.game.GameWorld}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and drops the current loot.
 * <li>{@link ActionResult#FAIL} cancels further processing and drops no loot.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 *
 * Listeners can modify the list of {@link ItemStack}s returned to them, regardless of what their result is.
 * If all listeners return {@link ActionResult#PASS}, the current loot is dropped.
 */
public interface EntityDropLootListener {
    EventType<EntityDropLootListener> EVENT = EventType.create(EntityDropLootListener.class, listeners -> (dropper, loot) -> {
        for (EntityDropLootListener listener : listeners) {
            TypedActionResult<List<ItemStack>> result = listener.onDropLoot(dropper, loot);

            // modify loot from listener (some may want to pass while still modifying loot)
            loot = result.getValue();

            // cancel early if success or fail was returned by the listener
            if (result.getResult() != ActionResult.PASS) {
                return result;
            }
        }

        return TypedActionResult.pass(loot);
    });

    TypedActionResult<List<ItemStack>> onDropLoot(LivingEntity dropper, List<ItemStack> loot);
}
