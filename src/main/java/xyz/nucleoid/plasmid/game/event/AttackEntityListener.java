package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public interface AttackEntityListener {
    EventType<AttackEntityListener> EVENT = EventType.create(AttackEntityListener.class, listeners -> {
        return (attacker, hand, attacked, hitResult) -> {
            for (AttackEntityListener listener : listeners) {
                ActionResult result = listener.onAttackEntity(attacker, hand, attacked, hitResult);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        };
    });

    ActionResult onAttackEntity(ServerPlayerEntity attacker, Hand hand, Entity attacked, EntityHitResult hitResult);
}
