package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.game.event.EventListeners;
import xyz.nucleoid.plasmid.game.event.EventType;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.GameRuleSet;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

/**
 * Represents the logic of a game in a {@link GameSpace} through events ({@link EventListeners}) and rules ({@link GameRuleSet}).
 *
 * <p>Each GameSpace contains one {@link GameLogic} instance instance at a time.
 * Games can be opened through {@link GameSpace#openGame}.
 */
public final class GameLogic {
    private final GameSpace space;

    private final EventListeners listeners = new EventListeners();
    private final GameRuleSet rules = new GameRuleSet();
    private final GameResources resources = new GameResources();

    GameLogic(GameSpace space) {
        this.space = space;
    }

    /**
     * Sets a global rule on this {@link GameLogic}.
     *
     * @param rule the rule type
     * @param result the behavior to be used for this rule
     * @return the updated {@link GameLogic}
     */
    public GameLogic setRule(GameRule rule, RuleResult result) {
        this.rules.put(rule, result);
        return this;
    }

    /**
     * Attaches a listener for the given event to this {@link GameLogic}
     *
     * @param event the event type to listen for
     * @param listener the listener to call when this event is invoked
     * @param <T> the event listener type
     * @return the updated {@link GameLogic}
     */
    public <T> GameLogic on(EventType<T> event, T listener) {
        this.listeners.add(event, listener);
        return this;
    }

    /**
     * Adds a resource to this {@link GameLogic} object that will be automatically closed when the {@link GameLogic}
     * instance is changed on the parent {@link GameSpace}.
     *
     * This differs from {@link GameSpace#addResource(AutoCloseable)}, which will be closed when the {@link GameSpace}
     * itself is closed.
     *
     * @param resource the resource to close when this {@link GameLogic} closes
     * @return the updated {@link GameLogic}
     */
    public GameLogic addResource(AutoCloseable resource) {
        this.resources.add(resource);
        return this;
    }

    /**
     * @return the parent {@link GameSpace} that this logic acts upon
     */
    public GameSpace getSpace() {
        return this.space;
    }

    /**
     * @return the {@link EventListeners} of this game
     */
    public EventListeners getListeners() {
        return this.listeners;
    }

    /**
     * @return the {@link GameRuleSet} of this game
     */
    public GameRuleSet getRules() {
        return this.rules;
    }

    public GameResources getResources() {
        return this.resources;
    }
}
