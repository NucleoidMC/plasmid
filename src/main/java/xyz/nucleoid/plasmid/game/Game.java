package xyz.nucleoid.plasmid.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import xyz.nucleoid.plasmid.game.event.EventType;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.GameRuleSet;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import java.util.HashMap;
import java.util.Map;

public final class Game {
    private final Multimap<EventType<?>, Object> listeners = HashMultimap.create();
    private final Map<EventType<?>, Object> invokers = new HashMap<>();

    private final GameRuleSet rules = new GameRuleSet();

    public Game setRule(GameRule rule, RuleResult result) {
        this.rules.put(rule, result);
        return this;
    }

    public <T> Game on(EventType<T> event, T listener) {
        this.listeners.put(event, listener);
        this.rebuildInvoker(event);

        return this;
    }

    private <T> void rebuildInvoker(EventType<T> event) {
        Object combined = event.combineUnchecked(this.listeners.get(event));
        this.invokers.put(event, combined);
    }

    Map<EventType<?>, Object> getInvokers() {
        return this.invokers;
    }

    GameRuleSet getRules() {
        return this.rules;
    }
}
