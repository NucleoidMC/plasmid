package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.GameSpace;

/**
 * Called when any {@link ServerPlayerEntity} is killed in a {@link GameSpace}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and kills the player.
 * <li>{@link ActionResult#FAIL} cancels further processing and does not kill the player.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 * <p>
 * If all listeners return {@link ActionResult#PASS}, the player is killed.
 */
public interface PlayerDeathListener {
    EventType<PlayerDeathListener> EVENT = EventType.create(PlayerDeathListener.class, listeners -> (player, source) -> {
        for (PlayerDeathListener listener : listeners) {
            ActionResult result = listener.onDeath(player, source);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onDeath(ServerPlayerEntity player, DamageSource source);
}
