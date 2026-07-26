package xyz.nucleoid.plasmid.impl.portal.game;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record NewGamePortalBackend(Holder<GameConfig<?>> game) implements GameConfigGamePortalBackend {
    @Override
    public void applyTo(ServerPlayer player, boolean alt) {
        CompletableFuture.supplyAsync(() -> this.openGame(player.level().getServer()))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
                    GameResult result;
                    if (gameSpace != null) {
                        result = GamePlayerJoiner.tryJoin(player, gameSpace, JoinIntent.PLAY);
                    } else {
                        result = GamePlayerJoiner.handleJoinException(throwable);
                    }

                    if (result.isError()) {
                        player.sendSystemMessage(result.errorCopy().withStyle(ChatFormatting.RED), false);
                    }

                    return null;
                }, player.level().getServer());
    }

    private CompletableFuture<GameSpace> openGame(MinecraftServer server) {
        return GameSpaceManagerImpl.get().open(this.game);
    }
}
