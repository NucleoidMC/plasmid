package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.game.player.JoinIntent;

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
                var result = GamePlayerJoiner.tryJoin(player, gameSpace, JoinIntent.ANY);

                if (result.isOk()) {
                    return;
                }
            }
        }

        CompletableFuture.supplyAsync(() -> this.getOrOpenNew(player.server))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
                    this.gameFuture = null;
                    GameResult result;
                    if (gameSpace != null) {
                        result = GamePlayerJoiner.tryJoin(player, gameSpace, JoinIntent.ANY);
                    } else {
                        result = GamePlayerJoiner.handleJoinException(throwable);
                    }

                    if (result.isError()) {
                        player.sendMessage(result.errorCopy().formatted(Formatting.RED), false);
                    }

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
