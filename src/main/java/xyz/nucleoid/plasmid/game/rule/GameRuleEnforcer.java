package xyz.nucleoid.plasmid.game.rule;

import net.minecraft.util.ActionResult;
import xyz.nucleoid.stimuli.event.EventRegistrar;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface GameRuleEnforcer {
    static <T> GameRuleEnforcer singleEvent(StimulusEvent<T> event, ListenerFactory<T> listenerFactory) {
        return (events, result) -> {
            events.listen(event, listenerFactory.create(result));
        };
    }

    void apply(EventRegistrar events, ActionResult result);

    interface ListenerFactory<T> {
        T create(ActionResult result);
    }
}
