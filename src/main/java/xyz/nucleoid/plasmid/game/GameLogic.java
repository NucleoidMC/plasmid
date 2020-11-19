package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.game.event.EventListeners;
import xyz.nucleoid.plasmid.game.event.EventType;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.GameRuleSet;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

/**
 * Represents the logic of a game in a {@link ManagedGameSpace} through events ({@link EventListeners}) and rules ({@link GameRuleSet}).
 *
 * <p>Each GameSpace contains one {@link GameLogic} instance instance at a time.
 * Games can be opened through {@link ManagedGameSpace#openGame}.
 */
public final class GameLogic {
    private final GameSpace world;

    private final EventListeners listeners = new EventListeners();
    private final GameRuleSet rules = new GameRuleSet();
    private final GameResources resources = new GameResources();

    GameLogic(GameSpace world) {
        this.world = world;
    }

    public GameLogic setRule(GameRule rule, RuleResult result) {
        this.rules.put(rule, result);
        return this;
    }

    public <T> GameLogic on(EventType<T> event, T listener) {
        this.listeners.add(event, listener);
        return this;
    }

    public GameLogic addResource(AutoCloseable resource) {
        this.resources.add(resource);
        return this;
    }

    public GameSpace getWorld() {
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
