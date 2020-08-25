package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.util.Scheduler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class OrderedGame implements GameLifecycle.Listeners {
    private final List<ConfiguredGame<?>> games;
    private int nextGameIndex;

    private List<ServerPlayerEntity> playersToTransfer;

    OrderedGame(List<ConfiguredGame<?>> games) {
        this.games = games;
    }

    public static CompletableFuture<GameWorld> open(GameOpenContext<CompositeGameConfig> context) {
        CompositeGameConfig config = context.getConfig();
        List<ConfiguredGame<?>> games = config.collectGames();

        if (games.isEmpty()) {
            throw new GameOpenException(new LiteralText("Composite game config is empty"));
        }

        OrderedGame ordered = new OrderedGame(games);
        return ordered.openNextGame(context.getServer());
    }

    @Nullable
    private CompletableFuture<GameWorld> openNextGame(MinecraftServer server) {
        ConfiguredGame<?> game = this.nextGame();
        if (game == null) {
            return null;
        }

        return game.open(server).thenApply(gameWorld -> {
            this.openNewGame(gameWorld);
            return gameWorld;
        });
    }

    @Nullable
    private ConfiguredGame<?> nextGame() {
        int index = this.nextGameIndex++;
        if (index >= this.games.size()) {
            return null;
        }

        return this.games.get(index);
    }

    private void openNewGame(GameWorld gameWorld) {
        if (this.playersToTransfer != null) {
            List<ServerPlayerEntity> playersToTransfer = this.playersToTransfer;
            this.playersToTransfer = null;

            Scheduler.INSTANCE.submit(server -> {
                for (ServerPlayerEntity player : playersToTransfer) {
                    gameWorld.addPlayer(player);
                }
            }, 10);
        }

        gameWorld.getLifecycle().addListeners(this);
    }

    @Override
    public void onClose(GameWorld gameWorld, List<ServerPlayerEntity> players) {
        this.playersToTransfer = new ArrayList<>(players);
        this.openNextGame(gameWorld.getWorld().getServer());
    }

    @Override
    public void onAddPlayer(GameWorld gameWorld, ServerPlayerEntity player) {
    }

    @Override
    public void onRemovePlayer(GameWorld gameWorld, ServerPlayerEntity player) {
    }
}
