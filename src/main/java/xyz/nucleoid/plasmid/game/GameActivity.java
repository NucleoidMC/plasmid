package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface GameActivity extends GameBehavior {
    /**
     * @return the parent {@link GameSpace} that this activity acts upon
     */
    GameSpace getGameSpace();

    /**
     * Sets a rule on this {@link GameActivity} that will be enforced while this activity is enabled.
     *
     * @param rule the rule type to set
     * @param result how this rule should be responded to
     * @return this {@link GameActivity}
     * @see GameActivity#allow(GameRuleType)
     * @see GameActivity#deny(GameRuleType)
     */
    GameActivity setRule(GameRuleType rule, EventResult result);

    /**
     * Sets a rule on this {@link GameActivity} to {@link EventResult#ALLOW} which will be enforced while this
     * activity is enabled.
     *
     * @param rule the rule type to set
     * @return this {@link GameActivity}
     * @see EventResult#ALLOW
     * @see GameActivity#setRule(GameRuleType, EventResult)
     */
    default GameActivity allow(GameRuleType rule) {
        return this.setRule(rule, EventResult.ALLOW);
    }

    /**
     * Sets a rule on this {@link GameActivity} to {@link EventResult#DENY} which will be enforced while this
     * activity is enabled.
     *
     * @param rule the rule type to set
     * @return this {@link GameActivity}
     * @see EventResult#DENY
     * @see GameActivity#setRule(GameRuleType, EventResult)
     */
    default GameActivity deny(GameRuleType rule) {
        return this.setRule(rule, EventResult.DENY);
    }

    /**
     * Registers a listener for the given event to this {@link GameActivity}.
     *
     * @param event the event type to listen for
     * @param listener the listener to call when this event is invoked
     * @param <T> the event listener type
     * @return the updated {@link GameActivity}
     */
    <T> GameActivity listen(StimulusEvent<T> event, T listener);

    /**
     * Adds a resource to this {@link GameActivity} object that will be automatically closed when the {@link GameActivity}
     * is destroyed.
     *
     * @param resource the resource to close when this {@link GameActivity} is destroyed
     * @return this {@link GameActivity}
     */
    GameActivity addResource(AutoCloseable resource);
}
