package xyz.nucleoid.plasmid.game.event;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;

import java.util.List;

/**
 * Called after an explosion is triggered in a {@link ManagedGameSpace}.
 */
public interface ExplosionListener {
    EventType<ExplosionListener> EVENT = EventType.create(ExplosionListener.class, listeners -> (affectedBlocks) -> {
        for (ExplosionListener listener : listeners) {
            listener.onExplosion(affectedBlocks);
        }
    });

    void onExplosion(List<BlockPos> affectedBlocks);
}
