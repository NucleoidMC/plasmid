package xyz.nucleoid.plasmid;

import com.google.common.reflect.Reflection;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.command.*;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.SimpleGameChannel;
import xyz.nucleoid.plasmid.game.composite.CompositeGame;
import xyz.nucleoid.plasmid.game.composite.CompositeGameConfig;
import xyz.nucleoid.plasmid.game.composite.RandomGame;
import xyz.nucleoid.plasmid.game.composite.RandomGameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.world.generator.VoidChunkGenerator;
import xyz.nucleoid.plasmid.item.IncludeEntityItem;
import xyz.nucleoid.plasmid.item.PlasmidItems;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspaceManager;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceBoundRenderer;
import xyz.nucleoid.plasmid.test.TestGame;

public final class Plasmid implements ModInitializer {
    public static final String ID = "plasmid";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        Reflection.initialize(PlasmidItems.class);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(ID, "void"), VoidChunkGenerator.CODEC);

        GameConfigs.register();
        MapTemplateSerializer.INSTANCE.register();

        GameChannel.register(new Identifier(ID, "simple"), SimpleGameChannel.CODEC);

        GameType.register(new Identifier(Plasmid.ID, "test"), TestGame::open, Codec.unit(Unit.INSTANCE));
        GameType.register(new Identifier(Plasmid.ID, "composite"), CompositeGame::open, CompositeGameConfig.CODEC);
        GameType.register(new Identifier(Plasmid.ID, "random"), RandomGame::open, RandomGameConfig.CODEC);

        // TODO: deprecate ordered id in favour of composite
        GameType.register(new Identifier(Plasmid.ID, "order"), CompositeGame::open, CompositeGameConfig.CODEC);

        this.registerCallbacks();
    }

    private void registerCallbacks() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            MapManageCommand.register(dispatcher);
            MapMetadataCommand.register(dispatcher);
            GameCommand.register(dispatcher);
            PartyCommand.register(dispatcher);
            ChatCommand.register(dispatcher);
            ShoutCommand.register(dispatcher);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity) {
                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
                    try {
                        UseItemListener invoker = gameSpace.invoker(UseItemListener.EVENT);
                        return invoker.onUseItem((ServerPlayerEntity) player, hand);
                    } catch (Exception e) {
                        LOGGER.error("An unexpected exception occurred while dispatching use item event", e);
                        gameSpace.reportError(e, "Use item");
                    }
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity) {
                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
                    try {
                        UseBlockListener invoker = gameSpace.invoker(UseBlockListener.EVENT);
                        return invoker.onUseBlock((ServerPlayerEntity) player, hand, hitResult);
                    } catch (Exception e) {
                        LOGGER.error("An unexpected exception occurred while dispatching use block event", e);
                        gameSpace.reportError(e, "Use block");
                    }
                }
            }

            return ActionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity) {
                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer((ServerPlayerEntity) player)) {
                    try {
                        BreakBlockListener invoker = gameSpace.invoker(BreakBlockListener.EVENT);
                        return invoker.onBreak((ServerPlayerEntity) player, pos) != ActionResult.FAIL;
                    } catch (Exception e) {
                        LOGGER.error("An unexpected exception occurred while dispatching block break event", e);
                        gameSpace.reportError(e, "Break block");
                    }
                }
            }
            return true;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
                if (gameSpace != null && gameSpace.containsPlayer(serverPlayer)) {
                    try {
                        AttackEntityListener invoker = gameSpace.invoker(AttackEntityListener.EVENT);
                        return invoker.onAttackEntity(serverPlayer, hand, entity, hitResult);
                    } catch (Exception e) {
                        LOGGER.error("An unexpected exception occurred while dispatching attack entity event", e);
                        gameSpace.reportError(e, "Attack entity");
                    }
                }
            }

            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem() instanceof IncludeEntityItem) {
                    BlockPos pos = player.getBlockPos();

                    MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(world.getServer());

                    return workspaceManager.getWorkspaces().stream()
                            .filter(workspace -> workspace.getBounds().contains(pos))
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
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            ManagedGameSpace game = ManagedGameSpace.forWorld(world);
            if (game != null) {
                try {
                    game.invoker(GameTickListener.EVENT).onTick();
                } catch (Exception e) {
                    LOGGER.error("An unexpected exception occurred while ticking the game", e);
                    game.reportError(e, "Ticking game");

                    game.closeWithError("An unexpected error occurred while ticking the game");
                }
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(WorkspaceBoundRenderer::onTick);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ManagedGameSpace gameSpace : ManagedGameSpace.getOpen()) {
                gameSpace.close(GameCloseReason.CANCELED);
            }
        });

        ServerWorldEvents.UNLOAD.register((server, world) -> {
            ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
            if (gameSpace != null) {
                gameSpace.close(GameCloseReason.CANCELED);
            }
        });
    }
}
