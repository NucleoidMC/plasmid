package xyz.nucleoid.plasmid;

import com.google.common.reflect.Reflection;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.command.*;
import xyz.nucleoid.plasmid.entity.CustomEntity;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.SimpleGameChannel;
import xyz.nucleoid.plasmid.game.composite.CompositeGameConfig;
import xyz.nucleoid.plasmid.game.composite.OrderedGame;
import xyz.nucleoid.plasmid.game.composite.RandomGame;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.event.AttackEntityListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.map.template.StagingBoundRenderer;
import xyz.nucleoid.plasmid.game.map.template.StagingMapManager;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.item.IncludeEntityItem;
import xyz.nucleoid.plasmid.item.PlasmidItems;
import xyz.nucleoid.plasmid.test.TestGame;

public final class Plasmid implements ModInitializer {
    public static final String ID = "plasmid";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        Reflection.initialize(PlasmidItems.class);

        GameConfigs.register();
        MapTemplateSerializer.INSTANCE.register();

        GameChannel.register(new Identifier(ID, "simple"), SimpleGameChannel.CODEC);

        GameType.register(new Identifier(Plasmid.ID, "test"), TestGame::open, Codec.unit(Unit.INSTANCE));
        GameType.register(new Identifier(Plasmid.ID, "order"), OrderedGame::open, CompositeGameConfig.CODEC);
        GameType.register(new Identifier(Plasmid.ID, "random"), RandomGame::open, CompositeGameConfig.CODEC);

        this.registerCallbacks();
    }

    private void registerCallbacks() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            MapCommand.register(dispatcher);
            GameCommand.register(dispatcher);
            PartyCommand.register(dispatcher);
            ChatCommand.register(dispatcher);
            ShoutCommand.register(dispatcher);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient) {
                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
                    if (gameSpace.testRule(GameRule.INTERACTION) == RuleResult.DENY) {
                        return TypedActionResult.fail(ItemStack.EMPTY);
                    }

                    UseItemListener invoker = gameSpace.invoker(UseItemListener.EVENT);
                    return invoker.onUseItem((ServerPlayerEntity) player, hand);
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient) {
                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
                    if (gameSpace.testRule(GameRule.INTERACTION) == RuleResult.DENY) {
                        return ActionResult.FAIL;
                    }

                    UseBlockListener invoker = gameSpace.invoker(UseBlockListener.EVENT);
                    return invoker.onUseBlock((ServerPlayerEntity) player, hand, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer(serverPlayer)) {
                    AttackEntityListener invoker = gameSpace.invoker(AttackEntityListener.EVENT);
                    return invoker.onAttackEntity(serverPlayer, hand, entity, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
                    if (gameSpace.testRule(GameRule.INTERACTION) == RuleResult.DENY) {
                        return ActionResult.FAIL;
                    }
                }

                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem() instanceof IncludeEntityItem) {
                    BlockPos pos = player.getBlockPos();

                    StagingMapManager stagingMapManager = StagingMapManager.get((ServerWorld) world);

                    return stagingMapManager.getStagingMaps().stream()
                            .filter(stagingMap -> stagingMap.getBounds().contains(pos))
                            .findFirst()
                            .map(map -> {
                                if (!map.getBounds().contains(entity.getBlockPos())) {
                                    player.sendMessage(
                                            new LiteralText("The targeted entity is not in the map \"" + map.getIdentifier() + "\".")
                                                    .formatted(Formatting.RED),
                                            false);
                                    return ActionResult.FAIL;
                                }

                                if (map.containsEntity(entity.getUuid())) {
                                    map.removeEntity(entity.getUuid());
                                    player.sendMessage(
                                            new LiteralText("The targeted entity has been removed from the map\"" + map.getIdentifier() + "\"."),
                                            true);
                                } else {
                                    map.addEntity(entity.getUuid());
                                    player.sendMessage(
                                            new LiteralText("The targeted entity has been added in the map\"" + map.getIdentifier() + "\"."),
                                            true);
                                }
                                return ActionResult.SUCCESS;
                            })
                            .orElseGet(() -> {
                                player.sendMessage(new LiteralText("You are not in any map.").formatted(Formatting.RED),
                                        false);
                                return ActionResult.FAIL;
                            });
                }

                CustomEntity customEntity = CustomEntity.match(entity);
                if (customEntity != null) {
                    return customEntity.interact(player, world, hand, entity, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            ManagedGameSpace game = ManagedGameSpace.forWorld(world);
            if (game != null) {
                game.invoker(GameTickListener.EVENT).onTick();
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(StagingBoundRenderer::onTick);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ManagedGameSpace gameSpace : ManagedGameSpace.getOpen()) {
                gameSpace.close();
            }
        });

        ServerWorldEvents.UNLOAD.register((server, world) -> {
            ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
            if (gameSpace != null) {
                gameSpace.close();
            }
        });
    }
}
