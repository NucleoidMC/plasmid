package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public interface UseBlockListener {
    EventType<UseBlockListener> EVENT = EventType.create(UseBlockListener.class, listeners -> {
        return (player, hand, hitResult) -> {
            for (UseBlockListener listener : listeners) {
                ActionResult result = listener.onUseBlock(player, hand, hitResult);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        };
    });

    ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult);
}
