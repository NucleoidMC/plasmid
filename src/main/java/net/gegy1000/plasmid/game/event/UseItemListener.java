package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public interface UseItemListener {
    EventType<UseItemListener> EVENT = EventType.create(UseItemListener.class, listeners -> {
        return (game, player, hand) -> {
            for (UseItemListener listener : listeners) {
                TypedActionResult<ItemStack> result = listener.onUseItem(game, player, hand);
                if (result.getResult() != ActionResult.PASS) {
                    return result;
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        };
    });

    TypedActionResult<ItemStack> onUseItem(Game game, ServerPlayerEntity player, Hand hand);
}
