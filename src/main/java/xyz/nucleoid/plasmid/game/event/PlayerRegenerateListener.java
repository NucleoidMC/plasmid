package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

/**
 * Called when a {@link ServerPlayerEntity} attempts to regenerate health naturally.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and allows the regeneration.
 * <li>{@link ActionResult#FAIL} cancels further processing and cancels the regeneration.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 * <p>
 * If all listeners return {@link ActionResult#PASS}, the player successfully regenerates health.
 */
public interface PlayerRegenerateListener {
    EventType<PlayerRegenerateListener> EVENT = EventType.create(PlayerRegenerateListener.class, listeners -> {
        return (player, amount) -> {
            for (PlayerRegenerateListener listener : listeners) {
                ActionResult result = listener.onRegenerate(player, amount);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        };
    });
    ActionResult onRegenerate(ServerPlayerEntity player, float amount);
}
