package xyz.nucleoid.plasmid.game.channel.simple;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.channel.GameChannelBackend;
import xyz.nucleoid.plasmid.game.channel.GameChannelMembers;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.player.JoinResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SimpleChannelBackend implements GameChannelBackend {
    private final Identifier gameId;

    private final GameChannelMembers members;

    private CompletableFuture<ManagedGameSpace> openGameFuture;

    public SimpleChannelBackend(Identifier gameId, GameChannelMembers members) {
        this.gameId = gameId;
        this.members = members;
    }

    @Override
    public Text getName() {
        ConfiguredGame<?> configuredGame = GameConfigs.get(this.gameId);
        if (configuredGame != null) {
            return new LiteralText(configuredGame.getName()).formatted(Formatting.AQUA);
        } else {
            return new LiteralText(this.gameId.toString()).formatted(Formatting.RED);
        }
    }

    @Override
    public CompletableFuture<JoinResult> requestJoin(ServerPlayerEntity player) {
        return this.getOpenGame(player.server)
                .thenCompose(gameSpace -> gameSpace.offerPlayer(player));
    }

    private CompletableFuture<ManagedGameSpace> getOpenGame(MinecraftServer server) {
        if (this.openGameFuture == null) {
            this.openGameFuture = this.openGame(server);
        }
        return this.openGameFuture;
    }

    private void onGameClose() {
        this.openGameFuture = null;
        this.members.clear();
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        ConfiguredGame<?> config = GameConfigs.get(this.gameId);
        if (config == null) {
            CompletableFuture<ManagedGameSpace> future = new CompletableFuture<>();
            TranslatableText error = new TranslatableText("Game config with id '%s' does not exist!", this.gameId);
            future.completeExceptionally(new GameOpenException(error));
            return future;
        }

        return config.open(server).thenApplyAsync(gameSpace -> {
            gameSpace.getLifecycle().addListeners(new LifecycleListeners());
            return gameSpace;
        }, server);
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            SimpleChannelBackend.this.members.addPlayer(player);
        }

        @Override
        public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            SimpleChannelBackend.this.members.removePlayer(player);
        }

        @Override
        public void onClose(GameSpace gameSpace, List<ServerPlayerEntity> players, GameCloseReason reason) {
            SimpleChannelBackend.this.onGameClose();
        }
    }
}
