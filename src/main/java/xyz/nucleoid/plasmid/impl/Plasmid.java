package xyz.nucleoid.plasmid.impl;

import com.google.common.reflect.Reflection;
import com.sun.net.httpserver.HttpServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.plasmid.api.event.GameEvents;
import xyz.nucleoid.plasmid.api.game.GameTypes;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProviderTypes;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.map.template.processor.MapTemplateProcessorTypes;
import xyz.nucleoid.plasmid.api.portal.GamePortalConfigs;
import xyz.nucleoid.plasmid.api.portal.menu.MenuEntryConfigs;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.command.*;
import xyz.nucleoid.plasmid.impl.compatibility.TrinketsCompatibility;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.portal.GamePortalInterface;
import xyz.nucleoid.plasmid.impl.portal.GamePortalManager;

public final class Plasmid implements ModInitializer {
    public static final String ID = "plasmid";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    private static HttpServer httpServer = null;

    @Override
    public void onInitialize() {
        PlasmidRegistries.registerDynamicRegistries();

        Reflection.initialize(GamePortalConfigs.class);
        Reflection.initialize(MenuEntryConfigs.class);
        Reflection.initialize(GameTypes.class);
        Reflection.initialize(TeamListProviderTypes.class);
        Reflection.initialize(MapTemplateProcessorTypes.class);

        this.registerCallbacks();

        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            TrinketsCompatibility.onInitialize();
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Plasmid.ID, path);
    }

    private void loadData(RegistryAccess registryManager, ResourceManager manager) {
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
                    player instanceof ServerPlayer serverPlayer
                            && entity instanceof GamePortalInterface portalInterface
                            && hand == InteractionHand.MAIN_HAND
            ) {
                if (portalInterface.interactWithPortal(serverPlayer)) {
                    return InteractionResult.SUCCESS_SERVER;
                }
            }

            return InteractionResult.PASS;
        });

        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            var game = GameSpaceManagerImpl.get().byLevel(world);
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
            loadData(server.registryAccess(), server.getResourceManager());
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
            loadData(server.registryAccess(), resourceManager);
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
