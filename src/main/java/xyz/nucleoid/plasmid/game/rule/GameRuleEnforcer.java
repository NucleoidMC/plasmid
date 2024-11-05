package xyz.nucleoid.plasmid.game.rule;

import xyz.nucleoid.stimuli.event.EventRegistrar;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface GameRuleEnforcer {
    static <T> GameRuleEnforcer singleEvent(StimulusEvent<T> event, ListenerFactory<T> listenerFactory) {
        return (events, result) -> {
            var listenerResult = result == EventResult.DENY ? EventResult.DENY : EventResult.PASS;
            events.listen(event, listenerFactory.create(listenerResult));
        };
    }

    void apply(EventRegistrar events, EventResult result);

    interface ListenerFactory<T> {
        T create(EventResult result);
    }
}
