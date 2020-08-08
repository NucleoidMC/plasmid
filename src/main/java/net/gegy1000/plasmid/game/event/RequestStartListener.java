package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.StartResult;

public interface RequestStartListener {
    EventType<RequestStartListener> EVENT = EventType.create(RequestStartListener.class, listeners -> {
        return () -> {
            for (RequestStartListener listener : listeners) {
                StartResult result = listener.requestStart();
                if (result != null) {
                    return result;
                }
            }
            return StartResult.alreadyStarted();
        };
    });

    StartResult requestStart();
}
