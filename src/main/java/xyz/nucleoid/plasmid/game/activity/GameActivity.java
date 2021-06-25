package xyz.nucleoid.plasmid.game.activity;

import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.GameBehavior;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.rule.GameRule;
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

    // TODO: should this exist here? name?
    GameActivitySource getSource();

    /**
     * Sets a rule on this {@link GameActivity} that will be enforced while this activity is active.
     *
     * @param rule the rule type
     * @param result the behavior to be used for this rule
     * @return the updated {@link GameActivity}
     */
    GameActivity setRule(GameRule rule, ActionResult result);

    default GameActivity allow(GameRule rule) {
        return this.setRule(rule, ActionResult.SUCCESS);
    }

    default GameActivity deny(GameRule rule) {
        return this.setRule(rule, ActionResult.FAIL);
    }

    /**
     * Attaches a listener for the given event to this {@link GameActivity}
     *
     * @param event the event type to listen for
     * @param listener the listener to call when this event is invoked
     * @param <T> the event listener type
     * @return the updated {@link GameActivity}
     */
    <T> GameActivity listen(StimulusEvent<T> event, T listener);

    /**
     * Adds a resource to this {@link GameActivity} object that will be automatically closed when the {@link GameActivity}
     * instance is changed on the parent {@link GameSpace}.
     *
     * @param resource the resource to close when this {@link GameActivity} closes
     * @return the updated {@link GameActivity}
     */
    GameActivity addResource(AutoCloseable resource);
}
