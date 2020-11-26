package xyz.nucleoid.plasmid.game.rule;

import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

// TODO: 0.5- games should directly work with leukocyte instead
public final class GameRule {
    public static final GameRule PORTALS = new GameRule(ProtectionRule.PORTALS);
    public static final GameRule CRAFTING = new GameRule(ProtectionRule.CRAFTING);
    public static final GameRule PVP = new GameRule(ProtectionRule.PVP);
    public static final GameRule HUNGER = new GameRule(ProtectionRule.HUNGER);
    public static final GameRule FALL_DAMAGE = new GameRule(ProtectionRule.FALL_DAMAGE);
    public static final GameRule INTERACTION = new GameRule(ProtectionRule.INTERACT_BLOCKS);
    public static final GameRule BLOCK_DROPS = new GameRule(ProtectionRule.BLOCK_DROPS);
    public static final GameRule THROW_ITEMS = new GameRule(ProtectionRule.THROW_ITEMS);
    public static final GameRule UNSTABLE_TNT = new GameRule(ProtectionRule.UNSTABLE_TNT);
    public static final GameRule TEAM_CHAT = new GameRule();

    private final ProtectionRule rule;

    private GameRule(ProtectionRule rule) {
        this.rule = rule;
    }

    public GameRule() {
        this(null);
    }

    @Nullable
    public ProtectionRule getRule() {
        return this.rule;
    }
}
