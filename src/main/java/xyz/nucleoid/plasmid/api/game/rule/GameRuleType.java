package xyz.nucleoid.plasmid.api.game.rule;

import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import xyz.nucleoid.stimuli.event.DroppedItemsResult;
import xyz.nucleoid.stimuli.event.EventRegistrar;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockDropItemsEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.block.CoralDeathEvent;
import xyz.nucleoid.stimuli.event.block.DispenserActivateEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;
import xyz.nucleoid.stimuli.event.item.ItemCraftEvent;
import xyz.nucleoid.stimuli.event.item.ItemPickupEvent;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerConsumeHungerEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerSwapWithOffhandEvent;
import xyz.nucleoid.stimuli.event.world.FireTickEvent;
import xyz.nucleoid.stimuli.event.world.FluidFlowEvent;
import xyz.nucleoid.stimuli.event.world.IceMeltEvent;
import xyz.nucleoid.stimuli.event.world.NetherPortalOpenEvent;

import java.util.Comparator;

public final class GameRuleType {
    public static final Comparator<GameRuleType> COMPARATOR = Comparator.comparing(type -> type.priority);

    public static final GameRuleType BREAK_BLOCKS = GameRuleType.create()
            .enforces(BlockBreakEvent.EVENT, result -> (player, world, pos) -> result);

    public static final GameRuleType PLACE_BLOCKS = GameRuleType.create()
            .enforces(BlockPlaceEvent.BEFORE, result -> (player, world, pos, state, ctx) -> result);

    public static final GameRuleType PORTALS = GameRuleType.create()
            .enforces(NetherPortalOpenEvent.EVENT, result -> (world, pos) -> result);

    public static final GameRuleType CRAFTING = GameRuleType.create()
            .enforces(ItemCraftEvent.EVENT, result -> (player, recipe) -> result);

    public static final GameRuleType PVP = GameRuleType.create()
            .enforces(PlayerDamageEvent.EVENT, result -> (player, source, amount) -> {
                if (source.getSource() instanceof PlayerEntity) {
                    return result;
                } else {
                    return EventResult.PASS;
                }
            });

    public static final GameRuleType HUNGER = GameRuleType.create()
            .enforces(PlayerConsumeHungerEvent.EVENT, result -> (player, foodLevel, saturation, exhaustion) -> result);

    public static final GameRuleType SATURATED_REGENERATION = GameRuleType.create();

    public static final GameRuleType FALL_DAMAGE = GameRuleType.create()
            .enforces(PlayerDamageEvent.EVENT, result -> (player, source, amount) -> {
                if (source.isIn(DamageTypeTags.IS_FALL)) {
                    return result;
                } else {
                    return EventResult.PASS;
                }
            });

    public static final GameRuleType USE_BLOCKS = GameRuleType.create()
            .enforces(BlockUseEvent.EVENT, result -> (player, hand, hitResult) -> result.asActionResult());
    public static final GameRuleType USE_ITEMS = GameRuleType.create()
            .enforces(ItemUseEvent.EVENT, result -> (player, hand) -> result.asActionResult());
    public static final GameRuleType USE_ENTITIES = GameRuleType.create()
            .enforces(EntityUseEvent.EVENT, result -> (player, entity, hand, hitResult) -> result);

    public static final GameRuleType INTERACTION = GameRuleType.allOf(USE_BLOCKS, USE_ITEMS, USE_ENTITIES);

    public static final GameRuleType BLOCK_DROPS = GameRuleType.create()
        .enforces(BlockDropItemsEvent.EVENT, result -> (entity, world, pos, state, drops) -> {
            return switch (result) {
                case PASS -> DroppedItemsResult.pass(drops);
                case ALLOW -> DroppedItemsResult.allow(drops);
                case DENY -> DroppedItemsResult.deny();
            };
        });

    public static final GameRuleType THROW_ITEMS = GameRuleType.create()
            .enforces(ItemThrowEvent.EVENT, result -> (player, slot, stack) -> result);
    public static final GameRuleType PICKUP_ITEMS = GameRuleType.create()
            .enforces(ItemPickupEvent.EVENT, result -> (player, slot, stack) -> result);

    public static final GameRuleType DISPENSER_ACTIVATE = GameRuleType.create()
            .enforces(DispenserActivateEvent.EVENT, result -> (world, pos, dispenser, slot, stack) -> result);

    public static final GameRuleType UNSTABLE_TNT = GameRuleType.create()
            .enforces(BlockPlaceEvent.AFTER, result -> (player, world, pos, state) -> {
                if (result != EventResult.DENY && state.getBlock() == Blocks.TNT) {
                    TntBlock.primeTnt(player.getWorld(), pos, player);
                    player.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            });

    public static final GameRuleType FIRE_TICK = GameRuleType.create()
            .enforces(FireTickEvent.EVENT, result -> (world, pos) -> result);
    public static final GameRuleType FLUID_FLOW = GameRuleType.create()
            .enforces(FluidFlowEvent.EVENT, result -> (world, fluidPos, fluidBlock, flowDirection, flowTo, flowToBlock) -> result);
    public static final GameRuleType ICE_MELT = GameRuleType.create()
            .enforces(IceMeltEvent.EVENT, result -> (world, pos) -> result);
    public static final GameRuleType CORAL_DEATH = GameRuleType.create()
            .enforces(CoralDeathEvent.EVENT, result -> (world, pos, from, to) -> result);

    public static final GameRuleType DISMOUNT_VEHICLE = GameRuleType.create();
    public static final GameRuleType STOP_SPECTATING_ENTITY = GameRuleType.create();
    public static final GameRuleType PLAYER_PROJECTILE_KNOCKBACK = GameRuleType.create();
    public static final GameRuleType TRIDENTS_LOYAL_IN_VOID = GameRuleType.create();
    public static final GameRuleType SPREAD_CONTAINER_LOOT = GameRuleType.create();
    public static final GameRuleType MODIFY_INVENTORY = GameRuleType.create();
    public static final GameRuleType MODIFY_ARMOR = GameRuleType.create();
    public static final GameRuleType SWAP_OFFHAND = GameRuleType.create().enforces(PlayerSwapWithOffhandEvent.EVENT, result -> (player) -> result);

    private GameRuleEnforcer enforcer;
    private Priority priority = Priority.NORMAL;

    private GameRuleType() {
    }

    public static GameRuleType create() {
        return new GameRuleType();
    }

    public static GameRuleType allOf(GameRuleType... rules) {
        return new GameRuleType()
                .enforcesAll(rules)
                .priority(Priority.LOW);
    }

    public GameRuleType enforces(GameRuleEnforcer enforcer) {
        this.enforcer = enforcer;
        return this;
    }

    public <T> GameRuleType enforces(StimulusEvent<T> event, GameRuleEnforcer.ListenerFactory<T> enforcer) {
        return this.enforces(GameRuleEnforcer.singleEvent(event, enforcer));
    }

    public GameRuleType enforcesAll(GameRuleType... rules) {
        return this.enforces((events, result) -> {
            for (var rule : rules) {
                rule.enforce(events, result);
            }
        });
    }

    public GameRuleType priority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public void enforce(EventRegistrar events, EventResult result) {
        var enforcer = this.enforcer;
        if (enforcer != null) {
            enforcer.apply(events, result);
        }
    }

    public enum Priority {
        HIGHEST,
        HIGH,
        NORMAL,
        LOW,
        LOWEST
    }
}
