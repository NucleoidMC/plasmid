package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.util.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OrderedGame implements GameLifecycle.Listeners {
    private final List<ConfiguredGame<?>> games;
    private int nextGameIndex;

    OrderedGame(List<ConfiguredGame<?>> games) {
        this.games = games;
    }

    public static GameOpenProcedure open(GameOpenContext<CompositeGameConfig> context) {
        CompositeGameConfig config = context.getConfig();
        List<ConfiguredGame<?>> games = config.collectGames();

        if (games.isEmpty()) {
            throw new GameOpenException(new LiteralText("Composite game config is empty"));
        }

        OrderedGame ordered = new OrderedGame(games);
        ConfiguredGame<?> game = Objects.requireNonNull(ordered.nextGame());

        return game.openProcedure(context.getServer())
                .then(logic -> logic.getSpace().getLifecycle().addListeners(ordered));
    }

    @Override
    public void onClose(GameSpace gameSpace, List<ServerPlayerEntity> players) {
        List<ServerPlayerEntity> playersToTransfer = new ArrayList<>(players);

        ConfiguredGame<?> game = this.nextGame();
        if (game == null) {
            return;
        }

        MinecraftServer server = gameSpace.getServer();
        game.open(server).thenAcceptAsync(newGameSpace -> {
            newGameSpace.getLifecycle().addListeners(this);

            Scheduler.INSTANCE.submit(s -> {
                for (ServerPlayerEntity player : playersToTransfer) {
                    newGameSpace.addPlayer(player);
                }
            }, 10);
        }, server);
    }

    @Nullable
    private ConfiguredGame<?> nextGame() {
        int index = this.nextGameIndex++;
        if (index >= this.games.size()) {
            return null;
        }

        return this.games.get(index);
    }
}
