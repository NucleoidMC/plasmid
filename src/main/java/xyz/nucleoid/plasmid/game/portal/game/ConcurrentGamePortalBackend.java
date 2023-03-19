package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ConcurrentGamePortalBackend implements GamePortalBackend {
    private final Identifier gameId;
    private CompletableFuture<ManagedGameSpace> gameFuture;

    public ConcurrentGamePortalBackend(Identifier gameId) {
        this.gameId = gameId;
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        var gameConfig = GameConfigs.get(this.gameId);
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig() == gameConfig) {
                consumer.accept(gameSpace);
            }
        }
    }

    @Override
    public int getPlayerCount() {
        int count = 0;
        var gameConfig = GameConfigs.get(this.gameId);
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig() == gameConfig) {
                count += gameSpace.getPlayers().size();
            }
        }
        return count;
    }

    @Override
    public List<Text> getDescription() {
        var config = GameConfigs.get(this.gameId);
        if (config != null) {
            return config.description();
        }

        return Collections.emptyList();
    }

    @Override
    public ItemStack getIcon() {
        var config = GameConfigs.get(this.gameId);
        if (config != null) {
            return config.icon();
        }

        return Items.BARRIER.getDefaultStack();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY;
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        var gameConfig = GameConfigs.get(this.gameId);
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig() == gameConfig) {
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

    @Override
    public Text getName() {
        var config = GameConfigs.get(this.gameId);
        if (config != null) {
            return GameConfig.name(config);
        } else {
            return Text.literal(this.gameId.toString()).formatted(Formatting.RED);
        }
    }

    public CompletableFuture<ManagedGameSpace> getOrOpenNew(MinecraftServer server) {
        var future = this.gameFuture;
        if (future == null || future.isCompletedExceptionally()) {
            this.gameFuture = future = this.openGame(server);
        }
        return future;
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        var config = GameConfigs.get(this.gameId);
        if (config == null) {
            Plasmid.LOGGER.warn("Missing game config for concurent game with id '{}'", this.gameId);

            var future = new CompletableFuture<ManagedGameSpace>();
            var error = Text.translatable("text.plasmid.game_config.game_config_does_not_exist", this.gameId);
            future.completeExceptionally(new GameOpenException(error));

            return future;
        }

        return GameSpaceManager.get().open(config);
    }
}
