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

    public static CompletableFuture<ManagedGameSpace> open(GameOpenContext<CompositeGameConfig> context) {
        CompositeGameConfig config = context.getConfig();
        List<ConfiguredGame<?>> games = config.collectGames();

        if (games.isEmpty()) {
            throw new GameOpenException(new LiteralText("Composite game config is empty"));
        }

        OrderedGame ordered = new OrderedGame(games);
        return ordered.openNextGame(context.getServer());
    }

    @Nullable
    private CompletableFuture<ManagedGameSpace> openNextGame(MinecraftServer server) {
        ConfiguredGame<?> game = this.nextGame();
        if (game == null) {
            return null;
        }

        return game.open(server).thenApply(gameSpace -> {
            this.openNewGame(gameSpace);
            return gameSpace;
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

    private void openNewGame(ManagedGameSpace gameSpace) {
        if (this.playersToTransfer != null) {
            List<ServerPlayerEntity> playersToTransfer = this.playersToTransfer;
            this.playersToTransfer = null;

            Scheduler.INSTANCE.submit(server -> {
                for (ServerPlayerEntity player : playersToTransfer) {
                    gameSpace.addPlayer(player);
                }
            }, 10);
        }

        gameSpace.getLifecycle().addListeners(this);
    }

    @Override
    public void onClose(ManagedGameSpace gameSpace, List<ServerPlayerEntity> players) {
        this.playersToTransfer = new ArrayList<>(players);
        this.openNextGame(gameSpace.getWorld().getServer());
    }

    @Override
    public void onAddPlayer(ManagedGameSpace gameSpace, ServerPlayerEntity player) {
    }

    @Override
    public void onRemovePlayer(ManagedGameSpace gameSpace, ServerPlayerEntity player) {
    }
}
