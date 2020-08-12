package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.game.event.EventListeners;
import xyz.nucleoid.plasmid.game.event.EventType;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.GameRuleSet;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

/**
 * Represents a game with event listeners ({@link EventListeners}) and a set of rules ({@link GameRuleSet}).
 *
 * <p>Each {@link GameWorld} contains a {@link Game} instance.
 */
public final class Game {
    private final EventListeners listeners = new EventListeners();
    private final GameRuleSet rules = new GameRuleSet();

    public Game setRule(GameRule rule, RuleResult result) {
        this.rules.put(rule, result);
        return this;
    }

    public <T> Game on(EventType<T> event, T listener) {
        this.listeners.add(event, listener);
        return this;
    }

    /**
     * Returns the {@link EventListeners} associated with this {@link Game}.
     *
     * @return the {@link EventListeners} of this {@link Game}
     */
    public EventListeners getListeners() {
        return this.listeners;
    }

    /**
     * Returns the {@link GameRuleSet} associated with this {@link Game}.
     *
     * @return the {@link GameRuleSet} of this {@link Game}
     */
    public GameRuleSet getRules() {
        return this.rules;
    }
}
