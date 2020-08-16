package xyz.nucleoid.plasmid.game.event;

public interface GameCloseListener {
    EventType<GameCloseListener> EVENT = EventType.create(GameCloseListener.class, listeners -> () -> {
        for (GameCloseListener listener : listeners) {
            listener.onClose();
        }
    });

    void onClose();
}
