package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

/**
 * Called when a {@link ServerPlayerEntity} fires an {@link ArrowEntity},
 * either with a bow or with a crossbow.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further handlers and executes vanilla behavior.
 * <li>{@link ActionResult#FAIL} cancels further handlers and does not execute vanilla behavior.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 */
public interface PlayerFireArrowListener {
    EventType<PlayerFireArrowListener> EVENT = EventType.create(PlayerFireArrowListener.class, listeners -> (user, tool, arrows, remaining, projectile) -> {
        for (PlayerFireArrowListener listener : listeners) {
            ActionResult result = listener.onFireArrow(user, tool, arrows, remaining, projectile);

            if (result != ActionResult.PASS) {
                System.out.println("Returning fail");
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onFireArrow(ServerPlayerEntity user, ItemStack tool, ArrowItem arrowItem, int remainingUseTicks, PersistentProjectileEntity projectile);
}
