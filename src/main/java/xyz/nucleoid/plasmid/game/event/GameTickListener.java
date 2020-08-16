package xyz.nucleoid.plasmid.game.event;

public interface GameTickListener {
    EventType<GameTickListener> EVENT = EventType.create(GameTickListener.class, listeners -> () -> {
        for (GameTickListener listener : listeners) {
            listener.onTick();
        }
    });

    void onTick();
}
