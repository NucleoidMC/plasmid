package xyz.nucleoid.plasmid.game.event;

public interface GameOpenListener {
    EventType<GameOpenListener> EVENT = EventType.create(GameOpenListener.class, listeners -> {
        return () -> {
            for (GameOpenListener listener : listeners) {
                listener.onOpen();
            }
        };
    });

    void onOpen();
}
