package xyz.nucleoid.plasmid.client.impl;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.InitialWorldOptions;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.FlatLevelGeneratorPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.client.impl.screen.GamesScreen;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.impl.game.manager.HasForcedGameSpace;

import java.util.Set;

public final class GameWorldCreator {
    public static final String WORLD_NAME = "PlasmidGame";
    public static final InitialWorldOptions OPTIONS = new InitialWorldOptions(WorldCreator.Mode.SURVIVAL, Set.of(), FlatLevelGeneratorPresets.THE_VOID);

    private GameWorldCreator() {
    }

    public static void create(MinecraftClient client, GeneratorOptionsHolder generatorOptionsHolder, RegistryEntry<GameConfig<?>> game) {
        var dimensionsConfig = generatorOptionsHolder.selectedDimensions()
            .toConfig(generatorOptionsHolder.dimensionOptionsRegistry());

        var registries = generatorOptionsHolder.combinedDynamicRegistries()
                .with(ServerDynamicRegistryType.DIMENSIONS, dimensionsConfig.toDynamicRegistryManager());

        var levelInfo = new LevelInfo(
                WORLD_NAME,
                GameMode.SURVIVAL,
                false,
                Difficulty.NORMAL,
                false,
                new GameRules(FeatureFlags.DEFAULT_ENABLED_FEATURES),
                generatorOptionsHolder.dataConfiguration()
        );

        var levelProperties = new LevelProperties(
                levelInfo,
                generatorOptionsHolder.generatorOptions(),
                dimensionsConfig.specialWorldProperty(),
                Lifecycle.stable()
        );

        CreateWorldScreen.showMessage(client, GamesScreen.PREPARING_MESSAGE);

        var session = CreateWorldScreen.createSession(client, levelInfo.getLevelName(), null);

        client.createIntegratedServerLoader().startNewWorld(
                session.orElseThrow(),
                generatorOptionsHolder.dataPackContents(),
                registries,
                levelProperties
        );

        // Server is created by now, so a game space can be created
        var server = client.getServer();
        var gameSpace = GameSpaceManagerImpl.get().open(game).join();

        ((HasForcedGameSpace) server).setForcedGameSpace(gameSpace);

        // Add existing players in the server to the game space just in case
        var players = server.getPlayerManager().getPlayerList();
        gameSpace.getPlayers().offer(players, JoinIntent.PLAY);
    }
}
