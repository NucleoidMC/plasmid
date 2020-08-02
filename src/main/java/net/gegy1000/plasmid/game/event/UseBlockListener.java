package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public interface UseBlockListener {
    EventType<UseBlockListener> EVENT = EventType.create(UseBlockListener.class, listeners -> {
        return (game, player, hand, hitResult) -> {
            for (UseBlockListener listener : listeners) {
                ActionResult result = listener.onUseBlock(game, player, hand, hitResult);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        };
    });

    ActionResult onUseBlock(Game game, ServerPlayerEntity player, Hand hand, BlockHitResult hitResult);
}
