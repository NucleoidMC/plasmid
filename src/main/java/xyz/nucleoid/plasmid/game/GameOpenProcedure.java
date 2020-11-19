package xyz.nucleoid.plasmid.game;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class GameOpenProcedure {
    private final MinecraftServer server;
    private final ConfiguredGame<?> game;

    private final BubbleWorldConfig worldConfig;
    private final Consumer<GameLogic> configureGame;

    GameOpenProcedure(MinecraftServer server, ConfiguredGame<?> game, BubbleWorldConfig worldConfig, Consumer<GameLogic> configureGame) {
        this.server = server;
        this.game = game;
        this.worldConfig = worldConfig;
        this.configureGame = configureGame;
    }

    public GameOpenProcedure then(Consumer<GameLogic> then) {
        return new GameOpenProcedure(
                this.server, this.game, this.worldConfig,
                game -> {
                    this.configureGame.accept(game);
                    then.accept(game);
                }
        );
    }

    public CompletableFuture<ManagedGameSpace> open() {
        return ManagedGameSpace.open(this.server, this.game, this.worldConfig)
                .thenApplyAsync(gameSpace -> {
                    gameSpace.openGame(this.configureGame);
                    return gameSpace;
                }, this.server);
    }
}
