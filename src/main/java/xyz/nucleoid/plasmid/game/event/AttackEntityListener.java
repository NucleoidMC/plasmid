package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/**
 * Called when any {@link ServerPlayerEntity} attempts to attack another {@link Entity}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and allows the attack.
 * <li>{@link ActionResult#FAIL} cancels further processing and cancels the attack.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 * <p>
 * If all listeners return {@link ActionResult#PASS}, the attack succeeds.
 */
public interface AttackEntityListener {
    EventType<AttackEntityListener> EVENT = EventType.create(AttackEntityListener.class, listeners -> (attacker, hand, attacked, hitResult) -> {
        for (AttackEntityListener listener : listeners) {
            ActionResult result = listener.onAttackEntity(attacker, hand, attacked, hitResult);

            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });

    ActionResult onAttackEntity(ServerPlayerEntity attacker, Hand hand, Entity attacked, EntityHitResult hitResult);
}
