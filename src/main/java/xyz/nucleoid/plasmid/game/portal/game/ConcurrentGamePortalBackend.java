package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.GamePlayerJoiner;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class ConcurrentGamePortalBackend implements GameConfigGamePortalBackend {
    private final RegistryEntry<GameConfig<?>> game;
    private CompletableFuture<ManagedGameSpace> gameFuture;

    public ConcurrentGamePortalBackend(RegistryEntry<GameConfig<?>> game) {
        this.game = game;
    }

    @Override
    public RegistryEntry<GameConfig<?>> game() {
        return this.game;
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig().equals(this.game)) {
                var results = GamePlayerJoiner.tryJoin(player, gameSpace);

                if (results.globalError == null && results.playerErrors.get(player) == null) {
                    return;
                }
            }
        }

        CompletableFuture.supplyAsync(() -> this.getOrOpenNew(player.server))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
                    this.gameFuture = null;
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

    public CompletableFuture<ManagedGameSpace> getOrOpenNew(MinecraftServer server) {
        var future = this.gameFuture;
        if (future == null || future.isCompletedExceptionally()) {
            this.gameFuture = future = this.openGame(server);
        }
        return future;
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        return GameSpaceManager.get().open(this.game);
    }
}
