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

public record NewGamePortalBackend(RegistryEntry<GameConfig<?>> game) implements GameConfigGamePortalBackend {
    @Override
    public void applyTo(ServerPlayerEntity player, boolean alt) {
        CompletableFuture.supplyAsync(() -> this.openGame(player.getEntityWorld().getServer()))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
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

    private CompletableFuture<GameSpace> openGame(MinecraftServer server) {
        return GameSpaceManagerImpl.get().open(this.game);
    }
}
