package xyz.nucleoid.plasmid.game.event;

import net.minecraft.world.explosion.Explosion;
import xyz.nucleoid.plasmid.game.GameSpace;

/**
 * Called after an explosion is triggered in a {@link GameSpace}.
 */
public interface ExplosionListener {
    EventType<ExplosionListener> EVENT = EventType.create(ExplosionListener.class, listeners -> explosion -> {
        for (ExplosionListener listener : listeners) {
            listener.onExplosion(explosion);
        }
    });

    void onExplosion(Explosion explosion, boolean particles);
}
