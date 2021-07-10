package xyz.nucleoid.plasmid.game;

import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface GameActivity extends GameBehavior {
    /**
     * @return the parent {@link GameSpace} that this activity acts upon
     */
    GameSpace getGameSpace();

    /**
     * @return the game config that created this activity
     */
    GameConfig<?> getGameConfig();

    /**
     * Sets a rule on this {@link GameActivity} that will be enforced while this activity is enabled.
     *
     * @param rule the rule type to set
     * @param result how this rule should be responded to
     * @return this {@link GameActivity}
     */
    GameActivity setRule(GameRuleType rule, ActionResult result);

    /**
     * Sets a rule on this {@link GameActivity} to {@link ActionResult#SUCCESS} which will be enforced while this
     * activity is enabled.
     *
     * @param rule the rule type to set
     * @return this {@link GameActivity}
     * @see ActionResult#SUCCESS
     * @see GameActivity#setRule(GameRuleType, ActionResult)
     */
    default GameActivity allow(GameRuleType rule) {
        return this.setRule(rule, ActionResult.SUCCESS);
    }

    /**
     * Sets a rule on this {@link GameActivity} to {@link ActionResult#FAIL} which will be enforced while this
     * activity is enabled.
     *
     * @param rule the rule type to set
     * @return this {@link GameActivity}
     * @see ActionResult#FAIL
     * @see GameActivity#setRule(GameRuleType, ActionResult)
     */
    default GameActivity deny(GameRuleType rule) {
        return this.setRule(rule, ActionResult.FAIL);
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
