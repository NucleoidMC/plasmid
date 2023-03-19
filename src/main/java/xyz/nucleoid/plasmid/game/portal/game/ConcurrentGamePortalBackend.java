package xyz.nucleoid.plasmid.game.portal.game;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ConcurrentGamePortalBackend implements GamePortalBackend {
    private final RegistryEntry<GameConfig<?>> game;
    private CompletableFuture<ManagedGameSpace> gameFuture;

    public ConcurrentGamePortalBackend(RegistryEntry<GameConfig<?>> game) {
        this.game = game;
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig().equals(this.game)) {
                consumer.accept(gameSpace);
            }
        }
    }

    @Override
    public int getPlayerCount() {
        int count = 0;
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig().equals(this.game)) {
                count += gameSpace.getPlayers().size();
            }
        }
        return count;
    }

    @Override
    public List<Text> getDescription() {
        return this.game.value().description();
    }

    @Override
    public ItemStack getIcon() {
        return this.game.value().icon();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY;
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        for (var gameSpace : GameSpaceManager.get().getOpenGameSpaces()) {
            if (gameSpace.getMetadata().sourceConfig().equals(this.game)) {
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
        return GameConfig.name(this.game);
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
