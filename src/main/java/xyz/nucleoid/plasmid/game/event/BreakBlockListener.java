package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * Called when any {@link ServerPlayerEntity} attempts to break a block.
 *
 * <p>This includes any {@link ServerPlayerEntity} attempting to trample farmland.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and allows the break.
 * <li>{@link ActionResult#FAIL} cancels further processing and cancels the break.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 * <p>
 * If all listeners return {@link ActionResult#PASS}, the break succeeds.
 */
public interface BreakBlockListener {
    EventType<BreakBlockListener> EVENT = EventType.create(BreakBlockListener.class, listeners -> (player, pos) -> {
        for (BreakBlockListener listener : listeners) {
            ActionResult result = listener.onBreak(player, pos);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onBreak(ServerPlayerEntity player, BlockPos pos);
}
