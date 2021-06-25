package xyz.nucleoid.plasmid.game.rule;

import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import xyz.nucleoid.stimuli.event.EventRegistrar;
import xyz.nucleoid.stimuli.event.StimulusEvent;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockDropItemsEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;
import xyz.nucleoid.stimuli.event.item.ItemCraftEvent;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerConsumeHungerEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.world.NetherPortalOpenEvent;

import java.util.ArrayList;

// TODO: re-evaluate all the rules we have + add more
// TODO: name this differently? confusion with vanilla
public final class GameRule {
    public static final GameRule BREAK_BLOCKS = GameRule.create()
            .enforces(BlockBreakEvent.EVENT, result -> (player, world, pos) -> result);

    public static final GameRule PLACE_BLOCKS = GameRule.create()
            .enforces(BlockPlaceEvent.BEFORE, result -> (player, world, pos, state, ctx) -> result);

    public static final GameRule PORTALS = GameRule.create()
            .enforces(NetherPortalOpenEvent.EVENT, result -> (world, pos) -> result);

    public static final GameRule CRAFTING = GameRule.create()
            .enforces(ItemCraftEvent.EVENT, result -> (player, recipe) -> result);

    public static final GameRule PVP = GameRule.create()
            .enforces(PlayerDamageEvent.EVENT, result -> (player, source, amount) -> {
                if (source.getSource() instanceof PlayerEntity) {
                    return result;
                } else {
                    return ActionResult.PASS;
                }
            });

    public static final GameRule HUNGER = GameRule.create()
            .enforces(PlayerConsumeHungerEvent.EVENT, result -> (player, foodLevel, saturation, exhaustion) -> result);

    public static final GameRule FALL_DAMAGE = GameRule.create()
            .enforces(PlayerDamageEvent.EVENT, result -> (player, source, amount) -> {
                if (source == DamageSource.FALL) {
                    return result;
                } else {
                    return ActionResult.PASS;
                }
            });

    public static final GameRule USE_BLOCKS = GameRule.create()
            .enforces(BlockUseEvent.EVENT, result -> (player, hand, hitResult) -> result);
    public static final GameRule USE_ITEMS = GameRule.create()
            .enforces(ItemUseEvent.EVENT, result -> (player, hand) -> new TypedActionResult<>(result, ItemStack.EMPTY));
    public static final GameRule USE_ENTITIES = GameRule.create()
            .enforces(EntityUseEvent.EVENT, result -> (player, entity, hand, hitResult) -> result);

    public static final GameRule INTERACTION = GameRule.allOf(USE_BLOCKS, USE_ITEMS, USE_ENTITIES);

    public static final GameRule BLOCK_DROPS = GameRule.create()
            .enforces(BlockDropItemsEvent.EVENT, result -> (entity, world, pos, state, drops) -> {
                if (result == ActionResult.FAIL) {
                    return TypedActionResult.fail(new ArrayList<>());
                } else {
                    return new TypedActionResult<>(result, drops);
                }
            });

    public static final GameRule THROW_ITEMS = GameRule.create()
            .enforces(ItemThrowEvent.EVENT, result -> (player, slot, stack) -> result);

    public static final GameRule UNSTABLE_TNT = GameRule.create()
            .enforces(BlockPlaceEvent.AFTER, result -> (player, world, pos, state) -> {
                if (result == ActionResult.SUCCESS && state.getBlock() == Blocks.TNT) {
                    TntBlock.primeTnt(player.world, pos);
                    player.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            });

    // TODO: make this not a rule?
    public static final GameRule TEAM_CHAT = GameRule.create();

    public static final GameRule DISMOUNT_VEHICLE = GameRule.create();
    public static final GameRule PLAYER_PROJECTILE_KNOCKBACK = GameRule.create();
    public static final GameRule TRIDENTS_LOYAL_IN_VOID = GameRule.create();
    public static final GameRule MODIFY_INVENTORY = GameRule.create();
    public static final GameRule MODIFY_ARMOR = GameRule.create();

    private GameRuleEnforcer enforcer;

    private GameRule() {
    }

    public static GameRule create() {
        return new GameRule();
    }

    public static GameRule allOf(GameRule... rules) {
        return new GameRule().enforcesAll(rules);
    }

    public GameRule enforces(GameRuleEnforcer enforcer) {
        this.enforcer = enforcer;
        return this;
    }

    public <T> GameRule enforces(StimulusEvent<T> event, GameRuleEnforcer.ListenerFactory<T> enforcer) {
        return this.enforces(GameRuleEnforcer.singleEvent(event, enforcer));
    }

    public GameRule enforcesAll(GameRule... rules) {
        return this.enforces((events, result) -> {
            for (GameRule rule : rules) {
                rule.enforce(events, result);
            }
        });
    }

    public void enforce(EventRegistrar events, ActionResult result) {
        GameRuleEnforcer enforcer = this.enforcer;
        if (enforcer != null) {
            enforcer.apply(events, result);
        }
    }
}
