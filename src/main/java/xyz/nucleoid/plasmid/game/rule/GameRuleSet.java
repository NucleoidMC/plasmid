package xyz.nucleoid.plasmid.game.rule;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

import java.util.Map;

// TODO: 0.5- games should directly work with leukocyte instead
public final class GameRuleSet {
    private Authority authority;
    private final Reference2ObjectMap<GameRule, RuleResult> rules = new Reference2ObjectOpenHashMap<>();

    public static GameRuleSet empty() {
        return new GameRuleSet();
    }

    @Deprecated
    public void setAuthority(Authority authority) {
        this.authority = authority;

        for (ProtectionRule rule : ProtectionRule.REGISTRY) {
            authority.rules.put(rule, xyz.nucleoid.leukocyte.rule.RuleResult.PASS);
        }

        for (Map.Entry<GameRule, RuleResult> entry : this.rules.entrySet()) {
            ProtectionRule authorityRule = entry.getKey().getRule();
            if (authorityRule != null) {
                authority.rules.put(authorityRule, entry.getValue().asLeukocyte());
            }
        }
    }

    public void put(GameRule rule, RuleResult value) {
        this.rules.put(rule, value);

        ProtectionRule authorityRule = rule.getRule();
        if (this.authority != null && authorityRule != null) {
            this.authority.rules.put(authorityRule, value.asLeukocyte());
        }
    }

    public RuleResult test(GameRule rule) {
        return this.rules.getOrDefault(rule, RuleResult.PASS);
    }
}
