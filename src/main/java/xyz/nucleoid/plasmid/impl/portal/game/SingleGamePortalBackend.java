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
import java.util.function.Function;

public final class SingleGamePortalBackend implements GameConfigGamePortalBackend {
    private final RegistryEntry<GameConfig<?>> game;
    private CompletableFuture<GameSpace> gameFuture;

    public SingleGamePortalBackend(RegistryEntry<GameConfig<?>> game) {
        this.game = game;
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        CompletableFuture.supplyAsync(() -> this.getOrOpen(player.server))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
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
