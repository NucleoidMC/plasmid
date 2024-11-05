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

public record NewGamePortalBackend(RegistryEntry<GameConfig<?>> game) implements GameConfigGamePortalBackend {
    @Override
    public void applyTo(ServerPlayerEntity player) {
        CompletableFuture.supplyAsync(() -> this.openGame(player.server))
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

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        return GameSpaceManager.get().open(this.game);
    }
}
