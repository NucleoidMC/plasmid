package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Called when a {@link net.minecraft.block.Block} is hit by a {@link net.minecraft.entity.projectile.ProjectileEntity}.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further handlers and executes vanilla behavior.
 * <li>{@link ActionResult#FAIL} cancels further handlers and does not execute vanilla behavior.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 */
public interface BlockHitListener {
    EventType<BlockHitListener> EVENT = EventType.create(BlockHitListener.class, listeners -> (entity, hitResult) -> {
        for (BlockHitListener listener : listeners) {
            ActionResult result = listener.onBlockHit(entity, hitResult);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onBlockHit(ProjectileEntity entity, BlockHitResult hitResult);
}
