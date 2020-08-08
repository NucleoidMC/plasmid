package net.gegy1000.plasmid.game.event;

public interface GameCloseListener {
    EventType<GameCloseListener> EVENT = EventType.create(GameCloseListener.class, listeners -> {
        return () -> {
            for (GameCloseListener listener : listeners) {
                listener.onClose();
            }
        };
    });

    void onClose();
}
