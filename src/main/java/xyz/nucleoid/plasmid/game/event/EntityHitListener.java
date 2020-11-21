package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;

/**
 * Called when a {@link net.minecraft.entity.LivingEntity} is hit by a {@link net.minecraft.entity.projectile.ProjectileEntity}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further handlers and executes vanilla behavior.
 * <li>{@link ActionResult#FAIL} cancels further handlers and does not execute vanilla behavior.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 */
public interface EntityHitListener {
    EventType<EntityHitListener> EVENT = EventType.create(EntityHitListener.class, listeners -> (entity, hitResult) -> {
        for (EntityHitListener listener : listeners) {
            ActionResult result = listener.onEntityHit(entity, hitResult);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onEntityHit(ProjectileEntity entity, EntityHitResult hitResult);
}
