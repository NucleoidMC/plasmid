package xyz.nucleoid.plasmid.game.manager;

import com.google.common.collect.Lists;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.player.isolation.IsolatingPlayerTeleporter;
import xyz.nucleoid.plasmid.game.resource_packs.ResourcePackStates;

import java.util.Collection;
import java.util.function.Consumer;

public final class ManagedGameSpace implements GameSpace {
    private final MinecraftServer server;
    private final GameSpaceManager manager;

    private final GameSpaceMetadata metadata;

    private final ManagedGameSpacePlayers players;
    private final ManagedGameSpaceWorlds worlds;

    private final GameLifecycle lifecycle = new GameLifecycle();

    private final long openTime;

    private final GameActivityState state = new GameActivityState(this);
    private final ResourcePackStates resourcePackStateManager = new ResourcePackStates(this);
    private boolean closed;

    private final GameSpaceStatistics statistics = new GameSpaceStatistics();

    private final IsolatingPlayerTeleporter teleporter;

    ManagedGameSpace(MinecraftServer server, GameSpaceManager manager, GameSpaceMetadata metadata) {
        this.server = server;
        this.manager = manager;

        this.metadata = metadata;

        this.players = new ManagedGameSpacePlayers(this);
        this.worlds = new ManagedGameSpaceWorlds(this);

        this.openTime = server.getOverworld().getTime();
        this.teleporter = new IsolatingPlayerTeleporter(server);
    }

    @Override
    public GameSpaceMetadata getMetadata() {
        return this.metadata;
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
        this.getPlayers().sendMessage(Text.literal(message).formatted(Formatting.RED));
        this.close(GameCloseReason.ERRORED);
    }

    @Override
    public void close(GameCloseReason reason) {
        if (this.closed) {
            return;
        }

        this.closed = true;

        var players = Lists.newArrayList(this.players);

        Plasmid.LOGGER.info("Game space {} (source: {}) closing for reason {}", this.metadata.id(), this.metadata.sourceConfig().source(), reason);
        GameEvents.CLOSING.invoker().onGameSpaceClosing(this, reason);
        this.lifecycle.onClosing(this, reason);

        try {
            this.state.closeActivity(reason);

            for (var player : players) {
                this.lifecycle.onRemovePlayer(this, player);

                this.teleporter.teleportOut(player);
            }
        } finally {
            for (var player : this.players) {
                this.manager.removePlayerFromGameSpace(this, player);
            }

            for (var world : this.worlds) {
                this.manager.removeDimensionFromGameSpace(this, world.getRegistryKey());
            }

            this.players.clear();
            this.worlds.clear();

            this.manager.removeGameSpace(this);

            this.lifecycle.onClosed(this, players, reason);
        }
    }

    @Override
    public GameSpacePlayers getPlayers() {
        return this.players;
    }

    @Override
    public ManagedGameSpaceWorlds getWorlds() {
        return this.worlds;
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
    public long getTime() {
        return this.server.getOverworld().getTime() - this.openTime;
    }

    @Override
    public GameSpaceStatistics getStatistics() {
        return this.statistics;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public ResourcePackStates getResourcePackStates() {
        return this.resourcePackStateManager;
    }

    public GameBehavior getBehavior() {
        return this.state;
    }

    @Override
    public GameResult screenJoins(Collection<ServerPlayerEntity> players) {
        if (this.closed) {
            return GameResult.error(GameTexts.Join.gameClosed());
        }

        return this.state.invoker(GamePlayerEvents.SCREEN_JOINS).screenJoins(players);
    }

    @Override
    public GameResult offer(ServerPlayerEntity player) {
        var result = this.attemptOffer(player);

        if (result.isError()) {
            this.attemptGarbageCollection();
        }

        return result;
    }

    private GameResult attemptOffer(ServerPlayerEntity player) {
        if (this.closed) {
            return GameResult.error(GameTexts.Join.gameClosed());
        } else if (this.players.contains(player)) {
            return GameResult.error(GameTexts.Join.alreadyJoined());
        } else if (this.manager.inGame(player)) {
            return GameResult.error(GameTexts.Join.inOtherGame());
        }

        var result = this.state.invoker(GamePlayerEvents.OFFER).onOfferPlayer(new PlayerOffer(player));

        var reject = result.asReject();
        if (reject != null) {
            return GameResult.error(reject.reason());
        }

        var accept = result.asAccept();
        if (accept != null) {
            try {
                this.acceptPlayer(player, accept);
                return GameResult.ok();
            } catch (Throwable throwable) {
                return GameResult.error(GameTexts.Join.unexpectedError());
            }
        } else {
            return GameResult.error(GameTexts.Join.genericError());
        }
    }

    private void acceptPlayer(ServerPlayerEntity player, PlayerOfferResult.Accept accept) {
        this.teleporter.teleportIn(player, accept::applyJoin);
        this.players.add(player);
        this.state.propagatingInvoker(GamePlayerEvents.JOIN).onAddPlayer(player);
        this.state.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);

        this.manager.addPlayerToGameSpace(this, player);

        this.lifecycle.onAddPlayer(this, player);

        var joinMessage = GameTexts.Join.success(player)
                .formatted(Formatting.YELLOW);
        this.players.sendMessage(joinMessage);

        GameEvents.PLAYER_JOIN.invoker().onPlayerJoin(this, player);
    }

    private void attemptGarbageCollection() {
        if (this.players.isEmpty()) {
            this.close(GameCloseReason.GARBAGE_COLLECTED);
        }
    }

    @Override
    public boolean kick(ServerPlayerEntity player) {
        if (this.players.remove(player)) {
            this.teleporter.teleportOut(player);
            return true;
        } else {
            return false;
        }
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        if (!this.players.contains(player)) {
            return false;
        }

        this.state.invoker(GamePlayerEvents.LEAVE).onRemovePlayer(player);
        this.state.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);

        this.lifecycle.onRemovePlayer(this, player);
        GameEvents.PLAYER_LEFT.invoker().onPlayerLeft(this, player);
        this.manager.removePlayerFromGameSpace(this, player);

        this.players.remove(player);
        this.attemptGarbageCollection();

        return true;
    }

    void onAddWorld(RuntimeWorldHandle worldHandle) {
        this.manager.addDimensionToGameSpace(this, worldHandle.asWorld().getRegistryKey());
    }

    void onRemoveWorld(RegistryKey<World> dimension) {
        this.manager.removeDimensionFromGameSpace(this, dimension);
    }

    public IsolatingPlayerTeleporter getTeleporter() {
        return this.teleporter;
    }
}
