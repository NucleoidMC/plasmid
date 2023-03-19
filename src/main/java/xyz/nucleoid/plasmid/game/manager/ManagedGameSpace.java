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
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.resource_packs.ResourcePackStates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    private final Map<String, Object> attachments = new HashMap<>();

    ManagedGameSpace(MinecraftServer server, GameSpaceManager manager, GameSpaceMetadata metadata) {
        this.server = server;
        this.manager = manager;

        this.metadata = metadata;

        this.players = new ManagedGameSpacePlayers(this);
        this.worlds = new ManagedGameSpaceWorlds(this);

        this.openTime = server.getOverworld().getTime();
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

        Plasmid.LOGGER.info("Game space {} (source: {}) closing for reason {}", this.metadata.id(), GameConfig.sourceName(this.metadata.sourceConfig()), reason);
        GameEvents.CLOSING.invoker().onGameSpaceClosing(this, reason);
        this.lifecycle.onClosing(this, reason);

        try {
            this.state.closeActivity(reason);

            for (var player : players) {
                this.lifecycle.onRemovePlayer(this, player);

                this.players.teleporter.teleportOut(player);
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
    public ManagedGameSpacePlayers getPlayers() {
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

    @Override
    public <T> T getAttachment(String key) {
        //noinspection unchecked
        return (T) this.attachments.get(key);
    }

    @Override
    public void setAttachment(String key, Object obj) {
        if (obj == null) {
            this.attachments.remove(key);
        } else {
            this.attachments.put(key, obj);
        }
    }

    public GameBehavior getBehavior() {
        return this.state;
    }

    GameResult screenJoins(Collection<ServerPlayerEntity> players) {
        var result = this.attemptScreenJoins(players);

        if (result.isError()) {
            this.players.attemptGarbageCollection();
        }

        return result;
    }

    private GameResult attemptScreenJoins(Collection<ServerPlayerEntity> players) {
        if (this.closed) {
            return GameResult.error(GameTexts.Join.gameClosed());
        }

        return this.state.invoker(GamePlayerEvents.SCREEN_JOINS).screenJoins(players);
    }

    PlayerOfferResult offerPlayer(PlayerOffer offer) {
        if (this.closed) {
            return offer.reject(GameTexts.Join.gameClosed());
        } else if (this.manager.inGame(offer.player())) {
            return offer.reject(GameTexts.Join.inOtherGame());
        }

        return this.state.invoker(GamePlayerEvents.OFFER).onOfferPlayer(offer);
    }

    void onAddPlayer(ServerPlayerEntity player) {
        this.state.propagatingInvoker(GamePlayerEvents.JOIN).onAddPlayer(player);
        this.state.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);

        this.manager.addPlayerToGameSpace(this, player);

        this.lifecycle.onAddPlayer(this, player);

        var joinMessage = GameTexts.Join.success(player)
                .formatted(Formatting.YELLOW);
        this.players.sendMessage(joinMessage);

        GameEvents.PLAYER_JOIN.invoker().onPlayerJoin(this, player);
    }

    void onPlayerRemove(ServerPlayerEntity player) {
        this.state.invoker(GamePlayerEvents.LEAVE).onRemovePlayer(player);
        this.state.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);

        this.lifecycle.onRemovePlayer(this, player);
        GameEvents.PLAYER_LEFT.invoker().onPlayerLeft(this, player);
        this.manager.removePlayerFromGameSpace(this, player);
    }

    void onAddWorld(RuntimeWorldHandle worldHandle) {
        this.manager.addDimensionToGameSpace(this, worldHandle.asWorld().getRegistryKey());
    }

    void onRemoveWorld(RegistryKey<World> dimension) {
        this.manager.removeDimensionFromGameSpace(this, dimension);
    }
}
