package xyz.nucleoid.plasmid.game.event;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * Called when any {@link ServerPlayerEntity} attempts to place a block.
 *
 * <p>Upon return:
 * <ul>
 * <li>{@link ActionResult#SUCCESS} cancels further processing and allows the place.
 * <li>{@link ActionResult#FAIL} cancels further processing and cancels the place.
 * <li>{@link ActionResult#PASS} moves on to the next listener.</ul>
 * <p>
 * If all listeners return {@link ActionResult#PASS}, the place succeeds.
 */
public interface PlaceBlockListener {
    EventType<PlaceBlockListener> EVENT = EventType.create(PlaceBlockListener.class, listeners -> (player, pos, state, context) -> {
        for (PlaceBlockListener listener : listeners) {
            ActionResult result = listener.onPlace(player, pos, state, context);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onPlace(ServerPlayerEntity player, BlockPos pos, BlockState state, ItemUsageContext context);
}
