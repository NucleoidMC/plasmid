package xyz.nucleoid.plasmid.game.composite;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.util.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CompositeGame implements GameLifecycle.Listeners {
    private final CompositeGameConfig config;

    private List<ConfiguredGame<?>> currentCycle;
    private int nextGameIndex;

    CompositeGame(CompositeGameConfig config) {
        this.config = config;
        this.currentCycle = this.buildCycle();
    }

    public static GameOpenProcedure open(GameOpenContext<CompositeGameConfig> context) {
        CompositeGameConfig config = context.getConfig();

        CompositeGame ordered = new CompositeGame(config);
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
        if (index >= this.currentCycle.size()) {
            if (this.config.isCyclic()) {
                this.currentCycle = this.buildCycle();
                this.nextGameIndex = 0;
                return this.currentCycle.get(this.nextGameIndex++);
            } else {
                return null;
            }
        }

        return this.currentCycle.get(index);
    }

    private List<ConfiguredGame<?>> buildCycle() {
        List<ConfiguredGame<?>> games = this.config.collectGames();
        if (this.config.isShuffled()) {
            Collections.shuffle(games);
        }

        if (!games.isEmpty()) {
            return games;
        } else {
            throw new GameOpenException(new LiteralText("Composite game config is empty"));
        }
    }
}
