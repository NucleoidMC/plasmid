package xyz.nucleoid.plasmid.game.channel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.player.JoinResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SimpleGameChannel implements GameChannel {
    public static final Codec<SimpleGameChannel> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(channel -> channel.id),
                Identifier.CODEC.fieldOf("game_id").forGetter(channel -> channel.gameId)
        ).apply(instance, SimpleGameChannel::new);
    });

    private final Identifier id;
    private final Identifier gameId;

    private final ChannelConnections connections = new ChannelConnections();

    private final LifecycleListeners lifecycleListeners = new LifecycleListeners();

    private ManagedGameSpace openGame;
    private CompletableFuture<ManagedGameSpace> openGameFuture;

    public SimpleGameChannel(Identifier id, Identifier gameId) {
        this.id = id;
        this.gameId = gameId;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public void requestJoin(ServerPlayerEntity player) {
        // TODO: needs better error handling: namely, CompletionException
        this.offerPlayer(player).handle((joinResult, throwable) -> {
            MutableText errorFeedback = null;

            if (joinResult != null && joinResult.isError()) {
                errorFeedback = joinResult.getError().shallowCopy();
            } else if (throwable != null) {
                if (throwable instanceof GameOpenException) {
                    errorFeedback = ((GameOpenException) throwable).getReason().shallowCopy();
                } else {
                    errorFeedback = new TranslatableText("text.plasmid.game.join.error");
                    Plasmid.LOGGER.warn("Failed to join game", throwable);
                }
            }

            if (errorFeedback != null) {
                player.sendMessage(errorFeedback.formatted(Formatting.RED), true);
            }

            return null;
        });
    }

    private CompletableFuture<JoinResult> offerPlayer(ServerPlayerEntity player) {
        return this.getOpenGame(player.server)
                .thenCompose(openGame -> openGame.offerPlayer(player));
    }

    private CompletableFuture<ManagedGameSpace> getOpenGame(MinecraftServer server) {
        if (this.openGame != null) {
            return CompletableFuture.completedFuture(this.openGame);
        }

        if (this.openGameFuture == null) {
            this.openGameFuture = this.openGame(server).thenApply(gameSpace -> {
                this.openGame(gameSpace);
                return gameSpace;
            });
        }

        return this.openGameFuture;
    }

    private void openGame(ManagedGameSpace gameSpace) {
        gameSpace.getLifecycle().addListeners(this.lifecycleListeners);

        this.openGame = gameSpace;
        this.openGameFuture = null;
        this.updateDisplay();
    }

    private void closeGame() {
        this.openGame = null;
        this.openGameFuture = null;
        this.updateDisplay();
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        ConfiguredGame<?> config = GameConfigs.get(this.gameId);
        if (config == null) {
            CompletableFuture<ManagedGameSpace> future = new CompletableFuture<>();
            TranslatableText error = new TranslatableText("Game config with id '%s' does not exist!", this.gameId);
            future.completeExceptionally(new GameOpenException(error));
            return future;
        }

        return config.open(server);
    }

    @Override
    public boolean connectTo(ChannelEndpoint endpoint) {
        return this.connections.connectTo(this, endpoint);
    }

    @Override
    public boolean removeConnection(ChannelEndpoint endpoint) {
        return this.connections.removeConnection(endpoint);
    }

    @Override
    public void invalidate() {
        this.connections.invalidate();
    }

    @Override
    public GameChannelDisplay display() {
        int playerCount = this.openGame != null ? this.openGame.getPlayerCount() : 0;

        String gameName;
        ConfiguredGame<?> configuredGame = GameConfigs.get(this.gameId);
        if (configuredGame != null) {
            gameName = configuredGame.getName();
        } else {
            gameName = this.gameId.toString();
        }

        return new GameChannelDisplay(new Text[] {
                new LiteralText(gameName).formatted(Formatting.BLUE),
                new LiteralText(playerCount + " players")
        });
    }

    private void updateDisplay() {
        this.connections.updateDisplay(this);
    }

    @Override
    public Codec<? extends GameChannel> getCodec() {
        return CODEC;
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            SimpleGameChannel.this.updateDisplay();
        }

        @Override
        public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            SimpleGameChannel.this.updateDisplay();
        }

        @Override
        public void onClose(GameSpace gameSpace, List<ServerPlayerEntity> players) {
            SimpleGameChannel.this.closeGame();
        }
    }
}
