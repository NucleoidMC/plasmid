package xyz.nucleoid.plasmid.impl.menu.entry;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An entry that puts the player into a game, under one of the {@link GameJoinMode join modes}.
 * <p>
 * Holds the pending game for the modes that reuse one, which is why entries are cached per registry id
 * rather than rebuilt for each menu that lists them.
 */
public final class GameEntry implements GameMenuEntry {
    private final Holder<GameConfig<?>> game;
    private final GameJoinMode mode;
    private final Component name;
    private final List<Component> description;
    private final ItemStack icon;

    private CompletableFuture<GameSpace> pending;

    public GameEntry(Holder<GameConfig<?>> game, GameJoinMode mode, Component name, List<Component> description, ItemStack icon) {
        this.game = game;
        this.mode = mode;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    @Override
    public Component name() {
        return this.name;
    }

    @Override
    public List<Component> description() {
        return this.description;
    }

    @Override
    public ItemStack icon() {
        return this.icon;
    }

    @Override
    public boolean isGameEntry() {
        return true;
    }

    @Override
    public Action getAction() {
        return Action.PLAY;
    }

    @Override
    public void click(ServerPlayer player, boolean alt) {
        var intent = alt ? JoinIntent.SPECTATE : JoinIntent.PLAY;

        if (this.mode == GameJoinMode.CONCURRENT) {
            for (var gameSpace : this.openGames()) {
                if (GamePlayerJoiner.tryJoin(player, gameSpace, intent).isOk()) {
                    return;
                }
            }
        }

        var server = player.level().getServer();

        CompletableFuture.supplyAsync(() -> this.obtain(server))
                .thenCompose(Function.identity())
                .handleAsync((gameSpace, throwable) -> {
                    if (this.mode != GameJoinMode.SINGLE) {
                        this.pending = null;
                    }

                    GameResult result = gameSpace != null
                            ? GamePlayerJoiner.tryJoin(player, gameSpace, intent)
                            : GamePlayerJoiner.handleJoinException(throwable);

                    if (result.isError()) {
                        player.sendSystemMessage(result.errorCopy().withStyle(ChatFormatting.RED), false);
                    }

                    return null;
                }, server);
    }

    private CompletableFuture<GameSpace> obtain(MinecraftServer server) {
        if (this.mode == GameJoinMode.NEW) {
            return GameSpaceManagerImpl.get().open(this.game);
        }

        var pending = this.pending;
        if (pending == null || pending.isCompletedExceptionally()) {
            this.pending = pending = GameSpaceManagerImpl.get().open(this.game);
        }

        return pending;
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        if (this.mode == GameJoinMode.SINGLE) {
            var pending = this.pending;

            if (pending != null && pending.isDone() && !pending.isCompletedExceptionally()) {
                consumer.accept(pending.join());
            }

            return;
        }

        this.openGames().forEach(consumer);
    }

    @Override
    public int getPlayerCount() {
        int count = 0;
        for (var gameSpace : this.openGames()) {
            count += gameSpace.getState().players();
        }
        return count;
    }

    @Override
    public int getSpectatorCount() {
        int count = 0;
        for (var gameSpace : this.openGames()) {
            count += gameSpace.getState().spectators();
        }
        return count;
    }

    private List<GameSpace> openGames() {
        return GameSpaceManagerImpl.get().getOpenGameSpaces().stream()
                .filter(gameSpace -> gameSpace.getMetadata().isSourceConfig(this.game))
                .map(GameSpace.class::cast)
                .toList();
    }
}
