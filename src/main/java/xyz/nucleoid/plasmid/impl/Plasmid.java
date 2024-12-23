package xyz.nucleoid.plasmid.impl;

import com.mojang.serialization.MapCodec;
import com.sun.net.httpserver.HttpServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.plasmid.api.event.GameEvents;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.impl.game.composite.RandomGame;
import xyz.nucleoid.plasmid.impl.game.composite.RandomGameConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfigs;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.portal.backend.game.ConcurrentGamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.backend.game.NewGamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.backend.game.SingleGamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.backend.menu.ActiveGamePortalBackend;
import xyz.nucleoid.plasmid.impl.portal.backend.menu.AdvancedMenuPortalBackend;
import xyz.nucleoid.plasmid.impl.portal.backend.menu.MenuPortalBackend;
import xyz.nucleoid.plasmid.impl.portal.config.GamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalInterface;
import xyz.nucleoid.plasmid.impl.portal.GamePortalManager;
import xyz.nucleoid.plasmid.impl.portal.config.*;
import xyz.nucleoid.plasmid.impl.command.*;
import xyz.nucleoid.plasmid.impl.compatibility.TrinketsCompatibility;
import xyz.nucleoid.plasmid.impl.portal.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.impl.portal.menu.*;

public final class Plasmid implements ModInitializer {
    public static final String ID = "plasmid";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    private static HttpServer httpServer = null;

    @Override
    public void onInitialize() {
        DynamicRegistries.register(GameConfigs.REGISTRY_KEY, GameConfig.REGISTRY_CODEC);

        GamePortalConfig.register(Identifier.of(ID, "single_game"), SingleGamePortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "new_game"), NewGamePortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "concurrent_game"), ConcurrentGamePortalConfig.CODEC);

        GamePortalConfig.register(Identifier.of(ID, "active_games"), ActiveGamePortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "menu"), MenuPortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "advanced_menu"), AdvancedMenuPortalConfig.CODEC);

        MenuEntryConfig.register(Identifier.of(ID, "game"), GameMenuEntryConfig.CODEC);
        MenuEntryConfig.register(Identifier.of(ID, "portal"), PortalEntryConfig.CODEC);

        GamePortalConfig.registerFactory(SingleGamePortalConfig.class, ((server, id, config) -> new SingleGamePortalBackend(config.game())));
        GamePortalConfig.registerFactory(NewGamePortalConfig.class, ((server, id, config) -> new NewGamePortalBackend(config.game())));
        GamePortalConfig.registerFactory(ConcurrentGamePortalConfig.class, ((server, id, config) -> new ConcurrentGamePortalBackend(config.game())));
        GamePortalConfig.registerFactory(MenuPortalConfig.class, ((server, id, config) -> {
            Text name;
            if (config.name() != null && config.name() != ScreenTexts.EMPTY) {
                name = config.name();
            } else {
                name = Text.literal(id.toString());
            }

            return new MenuPortalBackend(name, config.description(), config.icon(), config.games());
        }));
        GamePortalConfig.registerFactory(AdvancedMenuPortalConfig.class, ((server, id, config) -> {
            Text name;
            if (config.name() != null && config.name() != ScreenTexts.EMPTY) {
                name = config.name();
            } else {
                name = Text.literal(id.toString());
            }
            return new AdvancedMenuPortalBackend(name, config.description(), config.icon(), config.entries());
        }));

        GamePortalConfig.registerFactory(ActiveGamePortalConfig.class, ((server, id, config) -> {
            Text name;
            if (config.name() != null && config.name() != ScreenTexts.EMPTY) {
                name = config.name();
            } else {
                name = Text.literal(id.toString());
            }
            return new ActiveGamePortalBackend(name, config.description(), config.icon(), GameSpaceManager.get(), config.joinIntent());
        }));


        GameType.register(Identifier.of(Plasmid.ID, "random"), RandomGameConfig.CODEC, RandomGame::open);
        GameType.register(Identifier.of(Plasmid.ID, "invalid"), MapCodec.unit(""), (context) -> {
            var id = context.server().getRegistryManager().getOrThrow(GameConfigs.REGISTRY_KEY).getId(context.game());
            throw new GameOpenException(Text.translatable("text.plasmid.map.open.invalid_game", id != null ? id.toString() : context.game()));
        });

        this.registerCallbacks();

        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            TrinketsCompatibility.onInitialize();
        }
    }

    private void loadData(DynamicRegistryManager registryManager, ResourceManager manager) {
        GamePortalManager.INSTANCE.reload(registryManager, manager);
    }

    private void registerCallbacks() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            GameCommand.register(dispatcher);
            GamePortalCommand.register(dispatcher);
            ChatCommand.register(dispatcher);
            ShoutCommand.register(dispatcher);

            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                GameTestCommand.register(dispatcher);
            }
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (
                    player instanceof ServerPlayerEntity serverPlayer
                            && entity instanceof GamePortalInterface portalInterface
                            && hand == Hand.MAIN_HAND
            ) {
                if (portalInterface.interactWithPortal(serverPlayer)) {
                    return ActionResult.SUCCESS_SERVER;
                }
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            var game = GameSpaceManagerImpl.get().byWorld(world);
            if (game != null) {
                try {
                    game.getBehavior().propagatingInvoker(GameActivityEvents.TICK).onTick();
                } catch (Throwable t) {
                    game.closeWithError("An unexpected error occurred while ticking the game");
                }
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            GamePortalManager.INSTANCE.tick();
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            GameSpaceManagerImpl.openServer(server);
            GamePortalManager.INSTANCE.setup(server);
            loadData(server.getRegistryManager(), server.getResourceManager());
            PlasmidConfig.get().webServerConfig().ifPresent(config -> {
                httpServer = PlasmidWebServer.start(server, config);
            });
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            GameSpaceManagerImpl.startClosing();
            GamePortalManager.INSTANCE.close(server);
            if (httpServer != null) {
                httpServer.stop(0);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            GameSpaceManagerImpl.closeServer();
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, success) -> {
            loadData(server.getRegistryManager(), resourceManager);
        }));

        // For games to debug their statistic collection without needing to setup a backend
        if (Boolean.getBoolean("plasmid.debug_statistics")) {
            GameEvents.CLOSING.register((gameSpace, reason) -> {
                gameSpace.getStatistics().visitAll((name, bundle) -> {
                    LOGGER.info(bundle.encode().toString());
                });
            });
        }
    }
}
