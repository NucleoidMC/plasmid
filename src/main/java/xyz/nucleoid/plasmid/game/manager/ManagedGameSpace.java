package xyz.nucleoid.plasmid.game.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.activity.GameActivity;
import xyz.nucleoid.plasmid.game.activity.GameActivitySource;
import xyz.nucleoid.plasmid.game.activity.GameActivityStack;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.player.isolation.IsolatingPlayerTeleporter;
import xyz.nucleoid.plasmid.game.world.GameSpaceWorlds;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents the space within which a game occurs through attached {@link GameActivity}
 *
 * <p>Each world has a {@link net.minecraft.world.gen.chunk.ChunkGenerator} which is invoked as chunks are requested.
 * It is important to note that not all chunks will be loaded on start, and the game logic must take care to handle this.
 * Players can only be added to this game space through {@link GameSpace#offerPlayer}.
 */
// TODO: Go over the new 0.5 API and add proper documentation!

// TODO: Split up & simplify this class
public final class ManagedGameSpace implements GameSpace {
    private final MinecraftServer server;
    private final GameSpaceManager manager;
    private final MutablePlayerSet players;

    private final GameConfig<?> sourceConfig;
    private final Identifier id;

    private final GameSpaceWorlds worlds;
    private final GameActivityStack<ManagedGameActivity> activityStack = new GameActivityStack<>();
    private final IsolatingPlayerTeleporter playerTeleporter;

    private final GameLifecycle lifecycle = new GameLifecycle();

    private final long openTime;
    private boolean closed;

    ManagedGameSpace(MinecraftServer server, GameSpaceManager manager, GameConfig<?> sourceConfig, Identifier id) {
        this.server = server;
        this.players = new MutablePlayerSet(server);
        this.manager = manager;

        this.sourceConfig = sourceConfig;
        this.id = id;

        this.worlds = new GameSpaceWorlds(server);
        this.playerTeleporter = new IsolatingPlayerTeleporter(server);

        this.openTime = server.getOverworld().getTime();
    }

    @Override
    public ServerWorld addWorld(RuntimeWorldConfig worldConfig) {
        // TODO: 1.17: set default game rules on the config!
        RuntimeWorldHandle worldHandle = this.worlds.add(worldConfig);
        this.manager.addDimensionToGameSpace(this, worldHandle.asWorld().getRegistryKey());

        return worldHandle.asWorld();
    }

    @Override
    public void removeWorld(ServerWorld world) {
        RegistryKey<World> dimension = world.getRegistryKey();
        if (this.worlds.remove(dimension)) {
            this.manager.removeDimensionFromGameSpace(this, dimension);
        }
    }

    @Override
    public GameActivitySource activitySource(GameConfig<?> config) {
        return new GameActivitySource() {
            @Override
            public void push(Consumer<GameActivity> builder) {
                ManagedGameSpace.this.pushActivity(config, this, builder);
            }

            @Override
            public void swap(Consumer<GameActivity> builder) {
                ManagedGameSpace.this.swapActivity(config, this, builder);
            }

            @Override
            public void pop(GameActivity activity, GameCloseReason reason) {
                if (activity instanceof ManagedGameActivity) {
                    ManagedGameSpace.this.popActivity((ManagedGameActivity) activity, reason);
                }
            }
        };
    }

    void pushActivity(GameConfig<?> config, GameActivitySource source, Consumer<GameActivity> builder) {
        Preconditions.checkState(!this.closed, "GameSpace already closed!");

        ManagedGameActivity lastActivity = this.activityStack.peek();
        if (lastActivity != null) {
            this.disableActivity(lastActivity);
        }

        ManagedGameActivity activity = new ManagedGameActivity(this, config, source);
        builder.accept(activity);

        this.activityStack.push(activity);

        this.createActivity(activity);
        this.enableActivity(activity);
    }

    void swapActivity(GameConfig<?> config, GameActivitySource source, Consumer<GameActivity> builder) {
        Preconditions.checkState(!this.closed, "GameSpace already closed!");

        ManagedGameActivity closedActivity = this.activityStack.pop();
        if (closedActivity != null) {
            this.disableActivity(closedActivity);
            this.destroyActivity(closedActivity, GameCloseReason.SWAPPED);
        }

        ManagedGameActivity activity = new ManagedGameActivity(this, config, source);
        builder.accept(activity);

        this.activityStack.push(activity);

        this.createActivity(activity);
        this.enableActivity(activity);
    }

    void popActivity(ManagedGameActivity activity, GameCloseReason reason) {
        Preconditions.checkState(!this.closed, "GameSpace already closed!");

        if (this.activityStack.pop(activity)) {
            this.disableActivity(activity);
            this.destroyActivity(activity, reason);

            if (!this.activityStack.isEmpty()) {
                this.enableActivity(this.activityStack.peek());
            } else {
                this.close(reason);
            }
        } else {
            throw new IllegalArgumentException("Given GameActivity is not currently enabled!");
        }
    }

    private void createActivity(ManagedGameActivity activity) {
        try {
            activity.propagatingInvoker(GameActivityEvents.CREATE).onCreate();

            GameEvents.CREATE_ACTIVITY.invoker().onCreateActivity(this, activity);
        } catch (Throwable throwable) {
            this.closeWithError("An unexpected error occurred while opening the game");
        }
    }

    private void enableActivity(ManagedGameActivity activity) {
        for (ServerPlayerEntity player : this.players) {
            try {
                activity.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);
            } catch (Throwable throwable) {
                player.sendMessage(GameTexts.Join.unexpectedError(), false);
                this.kickPlayer(player);
            }
        }

        try {
            activity.propagatingInvoker(GameActivityEvents.ENABLE).onEnable();
        } catch (Throwable throwable) {
            this.closeWithError("An unexpected error occurred while resuming a game activity");
        }
    }

    private void disableActivity(ManagedGameActivity activity) {
        activity.invoker(GameActivityEvents.DISABLE).onDisable();
        for (ServerPlayerEntity player : this.players) {
            activity.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);
        }
    }

    private void destroyActivity(ManagedGameActivity activity, GameCloseReason reason) {
        activity.invoker(GameActivityEvents.DESTROY).onDestroy(reason);
        activity.onDestroy();

        GameEvents.DESTROY_ACTIVITY.invoker().onDestroyActivity(this, activity, reason);
    }

    @Override
    public GameResult screenPlayerJoins(Collection<ServerPlayerEntity> players) {
        if (this.closed) {
            return GameResult.error(GameTexts.Join.gameClosed());
        }

        return this.activityStack.invoker(GamePlayerEvents.SCREEN_JOINS).screenJoins(players);
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

        PlayerOffer offer = new PlayerOffer(player);
        PlayerOfferResult result = this.activityStack.invoker(GamePlayerEvents.OFFER).onOfferPlayer(offer);

        PlayerOfferResult.Reject reject = result.asReject();
        if (reject != null) {
            return GameResult.error(reject.getReason());
        }

        PlayerOfferResult.Accept accept = result.asAccept();
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

        this.activityStack.propagatingInvoker(GamePlayerEvents.JOIN).onAddPlayer(player);
        this.activityStack.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);

        this.players.add(player);
        this.manager.addPlayerToGameSpace(this, player);

        this.lifecycle.onAddPlayer(this, player);

        MutableText joinMessage = GameTexts.Join.success(player)
                .formatted(Formatting.YELLOW);
        this.getPlayers().sendMessage(joinMessage);
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
        if (!this.players.contains(player)) {
            return false;
        }

        this.activityStack.invoker(GamePlayerEvents.LEAVE).onRemovePlayer(player);
        this.activityStack.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);

        this.lifecycle.onRemovePlayer(this, player);

        this.players.remove(player);
        this.manager.removePlayerFromGameSpace(this, player);

        if (this.players.isEmpty()) {
            this.close(GameCloseReason.GARBAGE_COLLECTED);
        }

        return true;
    }

    @Override
    public GameResult requestStart() {
        GameResult startResult = GameEvents.START_REQUEST.invoker().onRequestStart(this, null);
        if (startResult != null) {
            return startResult;
        }

        startResult = this.activityStack.invoker(GameActivityEvents.REQUEST_START).onRequestStart();
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

    /**
     * Closes this {@link GameSpace} with the given reason.
     * All associated {@link GameActivity} instances are closed and all players will be removed.
     *
     * @param reason the reason for this game closing
     */
    public void close(GameCloseReason reason) {
        if (this.closed) {
            return;
        }

        this.closed = true;

        List<ServerPlayerEntity> players = Lists.newArrayList(this.players);

        GameEvents.CLOSING.invoker().onGameSpaceClosing(this, reason);
        this.lifecycle.onClosing(this, reason);

        try {
            ManagedGameActivity topActivity = this.activityStack.peek();
            if (topActivity != null) {
                this.disableActivity(topActivity);
            }

            ManagedGameActivity activity;
            while ((activity = this.activityStack.pop()) != null) {
                this.destroyActivity(activity, reason);
            }

            for (ServerPlayerEntity player : players) {
                this.lifecycle.onRemovePlayer(this, player);

                this.playerTeleporter.teleportOut(player);
            }
        } finally {
            Collection<RegistryKey<World>> dimensions = this.worlds.close();
            for (RegistryKey<World> dimension : dimensions) {
                this.manager.removeDimensionFromGameSpace(this, dimension);
            }

            for (ServerPlayerEntity player : players) {
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
    public Identifier getId() {
        return this.id;
    }

    @Override
    public long getTime() {
        return this.server.getOverworld().getTime() - this.openTime;
    }

    public GameBehavior getBehavior() {
        return this.activityStack;
    }

    public IsolatingPlayerTeleporter getPlayerTeleporter() {
        return this.playerTeleporter;
    }
}
