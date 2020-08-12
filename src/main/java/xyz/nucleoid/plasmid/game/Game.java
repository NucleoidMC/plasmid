package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.game.event.EventListeners;
import xyz.nucleoid.plasmid.game.event.EventType;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.GameRuleSet;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

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

    public EventListeners getListeners() {
        return this.listeners;
    }

    GameRuleSet getRules() {
        return this.rules;
    }
}
