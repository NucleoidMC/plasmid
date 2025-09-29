package xyz.nucleoid.plasmid.impl.portal.game;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class ConcurrentGamePortalBackend implements GameConfigGamePortalBackend {
    private final RegistryEntry<GameConfig<?>> game;
    private CompletableFuture<GameSpace> gameFuture;

    public ConcurrentGamePortalBackend(RegistryEntry<GameConfig<?>> game) {
        this.game = game;
    }

    @Override
    public RegistryEntry<GameConfig<?>> game() {
        return this.game;
    }

    @Override
    public void applyTo(ServerPlayerEntity player, boolean alt) {
        for (var gameSpace : GameSpaceManagerImpl.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig().equals(this.game)) {
                var result = GamePlayerJoiner.tryJoin(player, gameSpace, alt ? JoinIntent.SPECTATE : JoinIntent.PLAY);

                if (result.isOk()) {
                    return;
                }
            }
        }

        CompletableFuture.supplyAsync(() -> this.getOrOpenNew(player.getEntityWorld().getServer()))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
                    this.gameFuture = null;
                    GameResult result;
                    if (gameSpace != null) {
                        result = GamePlayerJoiner.tryJoin(player, gameSpace, JoinIntent.PLAY);
                    } else {
                        result = GamePlayerJoiner.handleJoinException(throwable);
                    }

                    if (result.isError()) {
                        player.sendMessage(result.errorCopy().formatted(Formatting.RED), false);
                    }

                    return null;
                }, player.getEntityWorld().getServer());
    }

    public CompletableFuture<GameSpace> getOrOpenNew(MinecraftServer server) {
        var future = this.gameFuture;
        if (future == null || future.isCompletedExceptionally()) {
            this.gameFuture = future = this.openGame(server);
        }
        return future;
    }

    private CompletableFuture<GameSpace> openGame(MinecraftServer server) {
        return GameSpaceManagerImpl.get().open(this.game);
    }
}
