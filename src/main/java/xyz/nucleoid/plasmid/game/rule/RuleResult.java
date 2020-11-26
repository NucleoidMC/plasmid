package xyz.nucleoid.plasmid.game.rule;

// TODO: 0.5- games should directly work with leukocyte instead
public enum RuleResult {
    ALLOW(xyz.nucleoid.leukocyte.rule.RuleResult.ALLOW),
    DENY(xyz.nucleoid.leukocyte.rule.RuleResult.DENY),
    PASS(xyz.nucleoid.leukocyte.rule.RuleResult.PASS);

    private final xyz.nucleoid.leukocyte.rule.RuleResult leukocyte;

    RuleResult(xyz.nucleoid.leukocyte.rule.RuleResult leukocyte) {
        this.leukocyte = leukocyte;
    }

    public xyz.nucleoid.leukocyte.rule.RuleResult asLeukocyte() {
        return this.leukocyte;
    }
}
