package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.game.event.EventListeners;
import xyz.nucleoid.plasmid.game.event.EventType;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.GameRuleSet;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

/**
 * Represents the logic of a game in a {@link GameWorld} through events ({@link EventListeners}) and rules ({@link GameRuleSet}).
 *
 * <p>Each GameWorld contains one {@link Game} instance instance at a time.
 * Games can be swapped or opened through {@link GameWorld#setGame} or {@link GameWorld#openGame}.
 */
public final class Game {
    private final EventListeners listeners = new EventListeners();
    private final GameRuleSet rules = new GameRuleSet();

    public static Game empty() {
        return new Game();
    }

    public Game setRule(GameRule rule, RuleResult result) {
        this.rules.put(rule, result);
        return this;
    }

    public <T> Game on(EventType<T> event, T listener) {
        this.listeners.add(event, listener);
        return this;
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
}
