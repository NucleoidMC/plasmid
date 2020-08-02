package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface ExplosionListener {
    EventType<ExplosionListener> EVENT = EventType.create(ExplosionListener.class, listeners -> {
        return (game, affectedBlocks) -> {
            for (ExplosionListener listener : listeners) {
                listener.onExplosion(game, affectedBlocks);
            }
        };
    });

    void onExplosion(Game game, List<BlockPos> affectedBlocks);
}
