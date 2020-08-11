package xyz.nucleoid.plasmid.game.rule;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public final class GameRuleSet {
    private final Reference2ObjectMap<GameRule, RuleResult> rules = new Reference2ObjectOpenHashMap<>();

    public static GameRuleSet empty() {
        return new GameRuleSet();
    }

    public void put(GameRule rule, RuleResult value) {
        this.rules.put(rule, value);
    }

    public RuleResult test(GameRule rule) {
        return this.rules.getOrDefault(rule, RuleResult.PASS);
    }
}
