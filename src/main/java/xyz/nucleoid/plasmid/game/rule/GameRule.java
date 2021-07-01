package xyz.nucleoid.plasmid.game.rule;

import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

// TODO: 0.5- games should directly work with leukocyte instead
public final class GameRule {
    public static final GameRule BREAK_BLOCKS = new GameRule(ProtectionRule.BREAK);
    public static final GameRule PLACE_BLOCKS = new GameRule(ProtectionRule.PLACE);
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
    public static final GameRule DISMOUNT_VEHICLE = new GameRule();
    public static final GameRule PLAYER_PROJECTILE_KNOCKBACK = new GameRule();
    public static final GameRule TRIDENTS_LOYAL_IN_VOID = new GameRule();
    public static final GameRule MODIFY_INVENTORY = new GameRule();
    public static final GameRule MODIFY_ARMOR = new GameRule();
    public static final GameRule FLUID_FLOW = new GameRule();

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
