package xyz.nucleoid.plasmid.game.manager;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.player.isolation.IsolatingPlayerTeleporter;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.game.world.GameSpaceWorlds;

import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ManagedGameSpace implements GameSpace {
    private final MinecraftServer server;
    private final GameSpaceManager manager;
    private final MutablePlayerSet players;

    private final GameConfig<?> sourceConfig;
    private final UUID id;
    private final Identifier userId;

    private final GameSpaceWorlds worlds;
    private final IsolatingPlayerTeleporter playerTeleporter;

    private final GameLifecycle lifecycle = new GameLifecycle();

    private final long openTime;

    private final GameActivityState state = new GameActivityState(this);
    private boolean closed;

    private final Object2ObjectMap<String, GameStatisticBundle> statistics = new Object2ObjectOpenHashMap<>();

    ManagedGameSpace(MinecraftServer server, GameSpaceManager manager, GameConfig<?> sourceConfig, UUID id, Identifier userId) {
        this.server = server;
        this.players = new MutablePlayerSet(server);
        this.manager = manager;

        this.sourceConfig = sourceConfig;
        this.id = id;
        this.userId = userId;

        this.worlds = new GameSpaceWorlds(server);
        this.playerTeleporter = new IsolatingPlayerTeleporter(server);

        this.openTime = server.getOverworld().getTime();
    }

    @Override
    public void setActivity(Consumer<GameActivity> builder) {
        try {
            this.state.setActivity(() -> {
                var activity = new ManagedGameActivity(this);
                builder.accept(activity);
                return activity;
            });
        } catch (Throwable throwable) {
            Plasmid.LOGGER.error("An unexpected error occurred while setting game activity", throwable);
            this.closeWithError("An unexpected error occurred while setting game activity");
        }
    }

    @Override
    public ServerWorld addWorld(RuntimeWorldConfig worldConfig) {
        var worldHandle = this.worlds.add(worldConfig);
        this.manager.addDimensionToGameSpace(this, worldHandle.asWorld().getRegistryKey());

        return worldHandle.asWorld();
    }

    @Override
    public void removeWorld(ServerWorld world) {
        var dimension = world.getRegistryKey();
        if (this.worlds.remove(dimension)) {
            this.manager.removeDimensionFromGameSpace(this, dimension);
        }
    }

    @Override
    public GameResult screenPlayerJoins(Collection<ServerPlayerEntity> players) {
        if (this.closed) {
            return GameResult.error(GameTexts.Join.gameClosed());
        }

        return this.state.invoker(GamePlayerEvents.SCREEN_JOINS).screenJoins(players);
    }

    @Override
    public GameResult offerPlayer(ServerPlayerEntity player) {
        if (this.closed) {
            return GameResult.error(GameTexts.Join.gameClosed());
        } else if (this.players.contains(player)) {
            return GameResult.error(GameTexts.Join.alreadyJoined());
        } else if (this.manager.inGame(player)) {
            return GameResult.error(GameTexts.Join.inOtherGame());
        }

        var offer = new PlayerOffer(player);
        var result = this.state.invoker(GamePlayerEvents.OFFER).onOfferPlayer(offer);

        var reject = result.asReject();
        if (reject != null) {
            return GameResult.error(reject.reason());
        }

        var accept = result.asAccept();
        if (accept != null) {
            try {
                this.addPlayer(player, accept);
                return GameResult.ok();
            } catch (Throwable throwable) {
                return GameResult.error(GameTexts.Join.unexpectedError());
            }
        } else {
            return GameResult.error(GameTexts.Join.genericError());
        }
    }

    private void addPlayer(ServerPlayerEntity player, PlayerOfferResult.Accept result) {
        this.playerTeleporter.teleportIn(player, result::applyJoin);

        this.state.propagatingInvoker(GamePlayerEvents.JOIN).onAddPlayer(player);
        this.state.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);

        this.players.add(player);
        this.manager.addPlayerToGameSpace(this, player);

        this.lifecycle.onAddPlayer(this, player);

        var joinMessage = GameTexts.Join.success(player)
                .formatted(Formatting.YELLOW);
        this.getPlayers().sendMessage(joinMessage);

        GameEvents.PLAYER_JOIN.invoker().onPlayerJoin(this, player);
    }

    @Override
    public boolean kickPlayer(ServerPlayerEntity player) {
        if (this.removePlayer(player)) {
            this.playerTeleporter.teleportOut(player);
            return true;
        } else {
            return false;
        }
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        if (this.closed || !this.players.contains(player)) {
            return false;
        }

        this.state.invoker(GamePlayerEvents.LEAVE).onRemovePlayer(player);
        this.state.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);

        this.lifecycle.onRemovePlayer(this, player);

        this.players.remove(player);
        this.manager.removePlayerFromGameSpace(this, player);

        if (this.players.isEmpty()) {
            this.close(GameCloseReason.GARBAGE_COLLECTED);
        }
        GameEvents.PLAYER_LEFT.invoker().onPlayerLeft(this, player);
        return true;
    }

    @Override
    public GameResult requestStart() {
        if (this.closed) {
            return GameResult.error(GameTexts.Start.alreadyStarted());
        }

        var startResult = GameEvents.START_REQUEST.invoker().onRequestStart(this, null);
        if (startResult != null) {
            return startResult;
        }

        startResult = this.state.invoker(GameActivityEvents.REQUEST_START).onRequestStart();
        if (startResult != null) {
            return startResult;
        } else {
            return GameResult.error(GameTexts.Start.genericError());
        }
    }

    public void closeWithError(String message) {
        this.getPlayers().sendMessage(new LiteralText(message).formatted(Formatting.RED));
        this.close(GameCloseReason.ERRORED);
    }

    @Override
    public void close(GameCloseReason reason) {
        if (this.closed) {
            return;
        }

        this.closed = true;

        var players = Lists.newArrayList(this.players);

        GameEvents.CLOSING.invoker().onGameSpaceClosing(this, reason);
        this.lifecycle.onClosing(this, reason);

        try {
            this.state.closeActivity(reason);

            for (var player : players) {
                this.lifecycle.onRemovePlayer(this, player);

                this.playerTeleporter.teleportOut(player);
            }
        } finally {
            var dimensions = this.worlds.close();
            for (var dimension : dimensions) {
                this.manager.removeDimensionFromGameSpace(this, dimension);
            }

            for (var player : players) {
                this.manager.removePlayerFromGameSpace(this, player);
            }

            this.players.clear();

            this.manager.removeGameSpace(this);

            this.lifecycle.onClosed(this, players, reason);
        }
    }

    @Override
    public PlayerSet getPlayers() {
        return this.players;
    }

    @Override
    public MinecraftServer getServer() {
        return this.server;
    }

    @Override
    public GameLifecycle getLifecycle() {
        return this.lifecycle;
    }

    @Override
    public GameConfig<?> getSourceConfig() {
        return this.sourceConfig;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public Identifier getUserId() {
        return this.userId;
    }

    @Override
    public long getTime() {
        return this.server.getOverworld().getTime() - this.openTime;
    }

    public GameBehavior getBehavior() {
        return this.state;
    }

    public IsolatingPlayerTeleporter getPlayerTeleporter() {
        return this.playerTeleporter;
    }

    @Override
    public GameStatisticBundle getStatistics(String namespace) {
        GameStatisticBundle.validateNamespace(namespace); // Will throw an exception if validation fails.
        return this.statistics.computeIfAbsent(namespace, __ -> new GameStatisticBundle());
    }

    @Override
    public void visitAllStatistics(BiConsumer<String, GameStatisticBundle> consumer) {
        for (var entry : this.statistics.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }
    }
}
