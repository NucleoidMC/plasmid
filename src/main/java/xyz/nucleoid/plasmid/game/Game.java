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
 * Games can be opened through {@link GameWorld#openGame}.
 */
public final class Game {
    private final GameWorld world;

    private final EventListeners listeners = new EventListeners();
    private final GameRuleSet rules = new GameRuleSet();
    private final GameResources resources = new GameResources();

    Game(GameWorld world) {
        this.world = world;
    }

    public Game setRule(GameRule rule, RuleResult result) {
        this.rules.put(rule, result);
        return this;
    }

    public <T> Game on(EventType<T> event, T listener) {
        this.listeners.add(event, listener);
        return this;
    }

    public Game addResource(AutoCloseable resource) {
        this.resources.add(resource);
        return this;
    }

    public GameWorld getWorld() {
        return this.world;
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
