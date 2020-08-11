package xyz.nucleoid.plasmid.game.event;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface ExplosionListener {
    EventType<ExplosionListener> EVENT = EventType.create(ExplosionListener.class, listeners -> {
        return (affectedBlocks) -> {
            for (ExplosionListener listener : listeners) {
                listener.onExplosion(affectedBlocks);
            }
        };
    });

    void onExplosion(List<BlockPos> affectedBlocks);
}
