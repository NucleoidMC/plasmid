package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;

/**
 * Called when any {@link net.minecraft.entity.LivingEntity} is killed in a {@link xyz.nucleoid.plasmid.game.GameWorld}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and kills the entity.
 * <li>{@link ActionResult#FAIL} cancels further processing and does not kill the entity.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 *
 * If all listeners return {@link ActionResult#PASS}, the entity is killed.
 */
public interface EntityDeathListener {
    EventType<EntityDeathListener> EVENT = EventType.create(EntityDeathListener.class, listeners -> (entity, source) -> {
        for (EntityDeathListener listener : listeners) {
            ActionResult result = listener.onDeath(entity, source);

            if(result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onDeath(LivingEntity entity, DamageSource source);
}
