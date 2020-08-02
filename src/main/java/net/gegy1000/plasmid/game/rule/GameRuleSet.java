package net.gegy1000.plasmid.game.rule;

import java.util.HashMap;
import java.util.Map;

public final class GameRuleSet {
    private final Map<GameRule, RuleResult> rules = new HashMap<>();

    public void put(GameRule rule, RuleResult value) {
        this.rules.put(rule, value);
    }

    public RuleResult test(GameRule rule) {
        return this.rules.getOrDefault(rule, RuleResult.PASS);
    }
}
