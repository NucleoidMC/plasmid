package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLifecycle;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.GamePlayerJoiner;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SingleGamePortalBackend implements GameConfigGamePortalBackend {
    private final RegistryEntry<GameConfig<?>> game;
    private CompletableFuture<ManagedGameSpace> gameFuture;

    public SingleGamePortalBackend(RegistryEntry<GameConfig<?>> game) {
        this.game = game;
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        CompletableFuture.supplyAsync(() -> this.getOrOpen(player.server))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
                    GamePlayerJoiner.Results results;
                    if (gameSpace != null) {
                        results = GamePlayerJoiner.tryJoin(player, gameSpace);
                    } else {
                        results = GamePlayerJoiner.handleJoinException(throwable);
                    }

                    results.sendErrorsTo(player);

                    return null;
                }, player.server);
    }

    public CompletableFuture<ManagedGameSpace> getOrOpen(MinecraftServer server) {
        var future = this.gameFuture;
        if (future == null) {
            this.gameFuture = future = this.openGame(server);
        }
        return future;
    }

    private void onClose() {
        this.gameFuture = null;
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        return GameSpaceManager.get().open(this.game).thenApplyAsync(gameSpace -> {
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
