package xyz.nucleoid.plasmid.game.event;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public interface UseItemListener {
    EventType<UseItemListener> EVENT = EventType.create(UseItemListener.class, listeners -> (player, hand) -> {
        for (UseItemListener listener : listeners) {
            TypedActionResult<ItemStack> result = listener.onUseItem(player, hand);
            if (result.getResult() != ActionResult.PASS) {
                return result;
            }
        }
        return TypedActionResult.pass(ItemStack.EMPTY);
    });

    TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand);
}
