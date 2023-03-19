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

public record NewGamePortalBackend(RegistryEntry<GameConfig<?>> game) implements GamePortalBackend {
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
    public ActionType getActionType() {
        return ActionType.PLAY;
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        CompletableFuture.supplyAsync(() -> this.openGame(player.server))
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
        return GameConfig.name(this.game);
    }

    @Override
    public List<Text> getDescription() {
        return this.game.value().description();
    }

    @Override
    public ItemStack getIcon() {
        return this.game.value().icon();
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        return GameSpaceManager.get().open(this.game);
    }
}
