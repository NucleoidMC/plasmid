package xyz.nucleoid.plasmid.game.event;

import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface GameEventExceptionHandler {
    <T> void handleException(StimulusEvent<T> event, Throwable throwable);
}
