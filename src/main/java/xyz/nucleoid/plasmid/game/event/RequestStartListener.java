package xyz.nucleoid.plasmid.game.event;

import xyz.nucleoid.plasmid.game.StartResult;

import javax.annotation.Nullable;

public interface RequestStartListener {
    EventType<RequestStartListener> EVENT = EventType.create(RequestStartListener.class, listeners -> {
        return () -> {
            for (RequestStartListener listener : listeners) {
                StartResult result = listener.requestStart();
                if (result != null) {
                    return result;
                }
            }
            return StartResult.ALREADY_STARTED;
        };
    });

    @Nullable
    StartResult requestStart();
}
