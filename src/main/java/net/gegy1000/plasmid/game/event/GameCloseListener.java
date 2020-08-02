package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;

public interface GameCloseListener {
    EventType<GameCloseListener> EVENT = EventType.create(GameCloseListener.class, listeners -> {
        return game -> {
            for (GameCloseListener listener : listeners) {
                listener.close(game);
            }
        };
    });

    void close(Game game);
}
