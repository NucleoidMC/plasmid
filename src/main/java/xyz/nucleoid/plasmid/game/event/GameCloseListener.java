package xyz.nucleoid.plasmid.game.event;

import xyz.nucleoid.plasmid.game.GameCloseReason;

public interface GameCloseListener {
    EventType<GameCloseListener> EVENT = EventType.create(GameCloseListener.class, listeners -> {
        return reason -> {
            for (GameCloseListener listener : listeners) {
                listener.onClose(reason);
            }
        };
    });

    void onClose(GameCloseReason reason);
}
