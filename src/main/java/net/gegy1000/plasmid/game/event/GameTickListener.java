package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;

public interface GameTickListener {
    EventType<GameTickListener> EVENT = EventType.create(GameTickListener.class, listeners -> {
        return game -> {
            for (GameTickListener listener : listeners) {
                listener.tick(game);
            }
        };
    });

    void tick(Game game);
}
