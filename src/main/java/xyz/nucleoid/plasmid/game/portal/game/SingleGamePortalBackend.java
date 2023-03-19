package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLifecycle;
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

public final class SingleGamePortalBackend implements GamePortalBackend {
    private final Identifier gameId;
    private CompletableFuture<ManagedGameSpace> gameFuture;

    public SingleGamePortalBackend(Identifier gameId) {
        this.gameId = gameId;
    }

    @Override
    public int getPlayerCount() {
        var future = this.gameFuture;
        if (future != null && future.isDone() && !future.isCompletedExceptionally()) {
            var game = future.join();
            return game.getPlayers().size();
        }
        return 0;
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        var future = this.gameFuture;
        if (future != null && future.isDone() && !future.isCompletedExceptionally()) {
            consumer.accept(future.join());
        }
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        CompletableFuture.supplyAsync(() -> this.getOrOpen(player.server))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
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

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY;
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

    public CompletableFuture<ManagedGameSpace> getOrOpen(MinecraftServer server) {
        var future = this.gameFuture;
        if (future == null) {
            this.gameFuture = future = this.openGame(server);
        }
        return future;
    }

    private void onClose() {
        this.gameFuture = null;
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        var config = GameConfigs.get(this.gameId);
        if (config == null) {
            Plasmid.LOGGER.warn("Missing game config for on-demand game with id '{}'", this.gameId);

            var future = new CompletableFuture<ManagedGameSpace>();
            var error = Text.translatable("text.plasmid.game_config.game_config_does_not_exist", this.gameId);
            future.completeExceptionally(new GameOpenException(error));

            return future;
        }

        return GameSpaceManager.get().open(config).thenApplyAsync(gameSpace -> {
            var lifecycle = gameSpace.getLifecycle();
            lifecycle.addListeners(new LifecycleListeners());

            return gameSpace;
        }, server);
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
            SingleGamePortalBackend.this.onClose();
        }
    }
}
