package xyz.nucleoid.plasmid.impl.game.manager;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.impl.player.LocalJoinAcceptor;
import xyz.nucleoid.plasmid.impl.player.LocalJoinOffer;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.api.event.GameEvents;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static xyz.nucleoid.plasmid.impl.Plasmid.id;

public final class ManagedGameSpace implements GameSpace {
    private final MinecraftServer server;
    private final GameSpaceManagerImpl manager;

    private final GameSpaceMetadata metadata;

    private final ManagedGameSpacePlayers players;
    private final ManagedGameSpaceLevels worlds;

    private final ArrayList<Predicate<PlayerRef>> playerFilters = new ArrayList<>();
    private final GameLifecycle lifecycle = new GameLifecycle();

    private final long openTime;

    private final GameActivityState state = new GameActivityState(this);
    private boolean closed;

    private final GameSpaceStatistics statistics = new GameSpaceStatistics();
    private final Map<GameAttachment<?>, Object> attachments = new Reference2ObjectOpenHashMap<>();

    ManagedGameSpace(MinecraftServer server, GameSpaceManagerImpl manager, GameSpaceMetadata metadata) {
        this.server = server;
        this.manager = manager;

        this.metadata = metadata;

        this.players = new ManagedGameSpacePlayers(this);
        this.worlds = new ManagedGameSpaceLevels(this);

        this.openTime = server.overworld().getGameTime();
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
            return GameResult.error(GameComponents.Start.alreadyStarted());
        }

        var startResult = GameEvents.START_REQUEST.invoker().onRequestStart(this, null);
        if (startResult != null) {
            return startResult;
        }

        startResult = this.state.invoker(GameActivityEvents.REQUEST_START).onRequestStart();
        if (startResult != null) {
            return startResult;
        } else {
            return GameResult.error(GameComponents.Start.genericError());
        }
    }

    public void closeWithError(String message) {
        this.getPlayers().sendMessage(Component.literal(message).withStyle(ChatFormatting.RED));
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
                this.manager.removeDimensionFromGameSpace(this, world.dimension());
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

    public List<Predicate<PlayerRef>> getPlayerFilters() {
        return Collections.unmodifiableList(this.playerFilters);
    }

    @Override
    public Predicate<PlayerRef> addPlayerFilter(Predicate<PlayerRef> filter) {
        this.playerFilters.add(filter);
        return filter;
    }

    @Override
    public void removePlayerFilter(Predicate<PlayerRef> filter) {
        this.playerFilters.remove(filter);
    }

    @Override
    public boolean isPlayerAllowed(PlayerRef player) {
        for (Predicate<PlayerRef> playerFilter : this.playerFilters) {
            if (!playerFilter.test(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ManagedGameSpaceLevels getLevels() {
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
        return this.server.overworld().getGameTime() - this.openTime;
    }

    @Override
    public GameSpaceStatistics getStatistics() {
        return this.statistics;
    }

    @Override
    public GameSpaceState getState() {
        return this.state.invoker(GameActivityEvents.STATE_UPDATE).onStateUpdate(new GameSpaceState.Builder(this));
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getAttachment(GameAttachment<? extends T> attachment) {
        return (T) this.attachments.get(attachment);
    }

    @Override
    public <T> void setAttachment(GameAttachment<? super T> attachment, @Nullable T value) {
        if (value == null) {
            this.attachments.remove(attachment);
        } else {
            this.attachments.put(attachment, value);
        }
    }

    @Override
    public GameBehavior getBehavior() {
        return this.state;
    }

    @Override
    public String toString() {
        return this.metadata.toString();
    }

    JoinOfferResult offerPlayers(LocalJoinOffer offer) {
        if (this.closed) {
            return offer.reject(GameComponents.Join.gameClosed());
        } else if (offer.serverPlayers().stream().anyMatch(this.manager::inGame)) {
            return offer.reject(GameComponents.Join.inOtherGame());
        } else if (offer.serverPlayers().stream().anyMatch(p -> !p.checkPermission(id("join_game"), true))) {
            return offer.reject(GameComponents.Join.notAllowed());
        }

        return this.state.invoker(GamePlayerEvents.OFFER).onOfferPlayers(offer);
    }

    JoinAcceptorResult acceptPlayers(LocalJoinAcceptor acceptor) {
        return this.state.invoker(GamePlayerEvents.ACCEPT).onAcceptPlayers(acceptor);
    }


    void onAddPlayer(ServerPlayer player) {
        this.state.propagatingInvoker(GamePlayerEvents.JOIN).onAddPlayer(player);
        this.state.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);

        this.manager.addPlayerToGameSpace(this, player);

        this.lifecycle.onAddPlayer(this, player);

        var spectator = this.players.spectators().contains(player);
        Component joinMessage = (spectator ? GameComponents.Join.successSpectator(player) : GameComponents.Join.success(player)).withStyle(ChatFormatting.YELLOW);
        joinMessage = this.state.invoker(GamePlayerEvents.JOIN_MESSAGE).onJoinMessageCreation(player, joinMessage, joinMessage);
        GameEvents.PLAYER_JOIN.invoker().onPlayerJoin(this, player);

        if (joinMessage != null) {
            this.players.sendMessage(joinMessage);
        }
    }

    void onPlayerRemove(ServerPlayer player) {
        var spectator = this.players.spectators().contains(player);
        Component leaveMessage = (spectator ? GameComponents.Leave.spectator(player) : GameComponents.Leave.participant(player)).withStyle(ChatFormatting.YELLOW);
        leaveMessage = this.state.invoker(GamePlayerEvents.LEAVE_MESSAGE).onLeaveMessageCreation(player, leaveMessage, leaveMessage);

        this.state.invoker(GamePlayerEvents.LEAVE).onRemovePlayer(player);
        this.state.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);

        this.lifecycle.onRemovePlayer(this, player);

        GameEvents.PLAYER_LEFT.invoker().onPlayerLeft(this, player);
        this.manager.removePlayerFromGameSpace(this, player);

        if (leaveMessage != null) {
            for (var receiver : this.players) {
                if (receiver != player) {
                    receiver.sendSystemMessage(leaveMessage);
                }
            }
        }
    }

    void onAddLevel(RuntimeLevelHandle worldHandle) {
        this.manager.addDimensionToGameSpace(this, worldHandle.asLevel().dimension());
    }

    void onRemoveLevel(ResourceKey<Level> dimension) {
        this.manager.removeDimensionFromGameSpace(this, dimension);
    }
}
