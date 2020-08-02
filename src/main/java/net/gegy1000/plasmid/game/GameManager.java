package net.gegy1000.plasmid.game;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GameManager {
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("game-manager")
                    .setDaemon(true)
                    .build()
    );

    private static State state = Closed.INSTANCE;

    @Nullable
    public static Closed closed() {
        if (state instanceof Closed) {
            return (Closed) state;
        } else {
            return null;
        }
    }

    @Nullable
    public static Open open() {
        if (state instanceof Open) {
            return (Open) state;
        } else {
            return null;
        }
    }

    @Nullable
    public static Game openGame() {
        if (state instanceof Open) {
            return ((Open) state).game;
        } else {
            return null;
        }
    }

    private interface State {
    }

    public static class Closed implements State {
        static final Closed INSTANCE = new Closed();

        Closed() {
        }

        public <C extends GameConfig> CompletableFuture<GameAndConfig<C>> open(MinecraftServer server, ConfiguredGame<C> configuredGame) {
            state = Opening.INSTANCE;
            return configuredGame.open(server).thenApply(game -> {
                state = new Open(game);
                return new GameAndConfig<>(game, configuredGame);
            });
        }
    }

    public static class Opening implements State {
        static final Opening INSTANCE = new Opening();

        Opening() {
        }
    }

    public static class Open implements State {
        private final Game game;

        public Open(Game game) {
            this.game = game;
        }

        public JoinResult offerPlayer(ServerPlayerEntity player) {
            return this.game.offerPlayer(player);
        }

        public StartResult requestStart() {
            StartResult result = this.game.requestStart();
            if (result.isOk()) {
                Game newGame = result.getGame();
                if (this.game != newGame) {
                    newGame.copyPlayersFrom(this.game);
                    state = new Open(newGame);
                }
            }
            return result;
        }

        public void close() {
            this.game.close();
            state = Closed.INSTANCE;
        }
    }
}
