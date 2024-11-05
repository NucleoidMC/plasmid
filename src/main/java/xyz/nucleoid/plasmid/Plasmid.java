package xyz.nucleoid.plasmid;

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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.plasmid.command.*;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.composite.RandomGame;
import xyz.nucleoid.plasmid.game.composite.RandomGameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.game.portal.GamePortalInterface;
import xyz.nucleoid.plasmid.game.portal.GamePortalManager;
import xyz.nucleoid.plasmid.game.portal.game.ConcurrentGamePortalConfig;
import xyz.nucleoid.plasmid.game.portal.game.LegacyOnDemandPortalConfig;
import xyz.nucleoid.plasmid.game.portal.game.NewGamePortalConfig;
import xyz.nucleoid.plasmid.game.portal.game.SingleGamePortalConfig;
import xyz.nucleoid.plasmid.game.portal.menu.*;
import xyz.nucleoid.plasmid.util.compatibility.TrinketsCompatibility;

public final class Plasmid implements ModInitializer {
    public static final String ID = "plasmid";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    private static HttpServer httpServer = null;

    @Override
    public void onInitialize() {
        DynamicRegistries.register(GameConfigs.REGISTRY_KEY, GameConfig.DIRECT_CODEC);

        GamePortalConfig.register(Identifier.of(ID, "single_game"), SingleGamePortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "new_game"), NewGamePortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "concurrent_game"), ConcurrentGamePortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "on_demand"), LegacyOnDemandPortalConfig.CODEC); // old one

        GamePortalConfig.register(Identifier.of(ID, "menu"), MenuPortalConfig.CODEC);
        GamePortalConfig.register(Identifier.of(ID, "advanced_menu"), AdvancedMenuPortalConfig.CODEC);

        MenuEntryConfig.register(Identifier.of(ID, "game"), GameMenuEntryConfig.CODEC);
        MenuEntryConfig.register(Identifier.of(ID, "portal"), PortalEntryConfig.CODEC);

        GameType.register(Identifier.of(Plasmid.ID, "random"), RandomGameConfig.CODEC, RandomGame::open);

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
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            var game = GameSpaceManager.get().byWorld(world);
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
            GameSpaceManager.openServer(server);
            GamePortalManager.INSTANCE.setup(server);
            loadData(server.getRegistryManager(), server.getResourceManager());
            PlasmidConfig.get().webServerConfig().ifPresent(config -> {
                httpServer = PlasmidWebServer.start(server, config);
            });
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            GameSpaceManager.startClosing();
            GamePortalManager.INSTANCE.close(server);
            if (httpServer != null) {
                httpServer.stop(0);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            GameSpaceManager.closeServer();
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
