package xyz.nucleoid.plasmid.impl.portal.game;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameLifecycle;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SingleGamePortalBackend implements GameConfigGamePortalBackend {
    private final RegistryEntry<GameConfig<?>> game;
    private CompletableFuture<GameSpace> gameFuture;

    public SingleGamePortalBackend(RegistryEntry<GameConfig<?>> game) {
        this.game = game;
    }

    @Override
    public void applyTo(ServerPlayerEntity player, boolean alt) {
        CompletableFuture.supplyAsync(() -> this.getOrOpen(player.getEntityWorld().getServer()))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
                    GameResult result;
                    if (gameSpace != null) {
                        result = GamePlayerJoiner.tryJoin(player, gameSpace, alt ? JoinIntent.SPECTATE : JoinIntent.PLAY);
                    } else {
                        result = GamePlayerJoiner.handleJoinException(throwable);
                    }

                    if (result.isError()) {
                        player.sendMessage(result.errorCopy().formatted(Formatting.RED), false);
                    }

                    return null;
                }, player.getEntityWorld().getServer());
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        if (this.gameFuture != null && this.gameFuture.isDone()) {
            try {
                consumer.accept(this.gameFuture.get());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getPlayerCount() {
        if (this.gameFuture != null && this.gameFuture.isDone()) {
            try {
                return this.gameFuture.get().getState().players();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    @Override
    public int getSpectatorCount() {
        if (this.gameFuture != null && this.gameFuture.isDone()) {
            try {
                return this.gameFuture.get().getState().spectators();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    @Override
    public int getMaxPlayerCount() {
        if (this.gameFuture != null && this.gameFuture.isDone()) {
            try {
                return this.gameFuture.get().getState().maxPlayers();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    public CompletableFuture<GameSpace> getOrOpen(MinecraftServer server) {
        var future = this.gameFuture;
        if (future == null) {
            this.gameFuture = future = this.openGame(server);
        }
        return future;
    }

    private void onClose() {
        this.gameFuture = null;
    }

    private CompletableFuture<GameSpace> openGame(MinecraftServer server) {
        return GameSpaceManagerImpl.get().open(this.game).thenApplyAsync(gameSpace -> {
            var lifecycle = gameSpace.getLifecycle();
            lifecycle.addListeners(new LifecycleListeners());

            return gameSpace;
        }, server);
    }

    @Override
    public RegistryEntry<GameConfig<?>> game() {
        return this.game;
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
            SingleGamePortalBackend.this.onClose();
        }
    }
}
