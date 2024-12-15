package xyz.nucleoid.plasmid.impl.game.manager;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.impl.player.LocalJoinAcceptor;
import xyz.nucleoid.plasmid.impl.player.LocalJoinOffer;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.api.event.GameEvents;

import java.util.Map;
import java.util.function.Consumer;

public final class ManagedGameSpace implements GameSpace {
    private final MinecraftServer server;
    private final GameSpaceManagerImpl manager;

    private final GameSpaceMetadata metadata;

    private final ManagedGameSpacePlayers players;
    private final ManagedGameSpaceWorlds worlds;

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

    JoinOfferResult offerPlayers(LocalJoinOffer offer) {
        if (this.closed) {
            return offer.reject(GameTexts.Join.gameClosed());
        } else if (offer.serverPlayers().stream().anyMatch(this.manager::inGame)) {
            return offer.reject(GameTexts.Join.inOtherGame());
        } else if (offer.serverPlayers().stream().anyMatch(p -> !Permissions.check(p, "plasmid.join_game", true))) {
            return offer.reject(GameTexts.Join.notAllowed());
        }

        return this.state.invoker(GamePlayerEvents.OFFER).onOfferPlayers(offer);
    }

    JoinAcceptorResult acceptPlayers(LocalJoinAcceptor acceptor) {
        return this.state.invoker(GamePlayerEvents.ACCEPT).onAcceptPlayers(acceptor);
    }


    void onAddPlayer(ServerPlayerEntity player) {
        this.state.propagatingInvoker(GamePlayerEvents.JOIN).onAddPlayer(player);
        this.state.propagatingInvoker(GamePlayerEvents.ADD).onAddPlayer(player);

        this.manager.addPlayerToGameSpace(this, player);

        this.lifecycle.onAddPlayer(this, player);

        var spectator = this.players.spectators().contains(player);
        Text joinMessage = (spectator ? GameTexts.Join.successSpectator(player) : GameTexts.Join.success(player)).formatted(Formatting.YELLOW);
        joinMessage = this.state.invoker(GamePlayerEvents.JOIN_MESSAGE).onJoinMessageCreation(player, joinMessage, joinMessage);
        GameEvents.PLAYER_JOIN.invoker().onPlayerJoin(this, player);

        if (joinMessage != null) {
            this.players.sendMessage(joinMessage);
        }
    }

    void onPlayerRemove(ServerPlayerEntity player) {
        var spectator = this.players.spectators().contains(player);
        Text leaveMessage = (spectator ? GameTexts.Leave.spectator(player) : GameTexts.Leave.participant(player)).formatted(Formatting.YELLOW);
        leaveMessage = this.state.invoker(GamePlayerEvents.LEAVE_MESSAGE).onLeaveMessageCreation(player, leaveMessage, leaveMessage);

        this.state.invoker(GamePlayerEvents.LEAVE).onRemovePlayer(player);
        this.state.invoker(GamePlayerEvents.REMOVE).onRemovePlayer(player);

        this.lifecycle.onRemovePlayer(this, player);

        GameEvents.PLAYER_LEFT.invoker().onPlayerLeft(this, player);
        this.manager.removePlayerFromGameSpace(this, player);

        if (leaveMessage != null) {
            for (var receiver : this.players) {
                if (receiver != player) {
                    receiver.sendMessage(leaveMessage);
                }
            }
        }
    }

    void onAddWorld(RuntimeWorldHandle worldHandle) {
        this.manager.addDimensionToGameSpace(this, worldHandle.asWorld().getRegistryKey());
    }

    void onRemoveWorld(RegistryKey<World> dimension) {
        this.manager.removeDimensionFromGameSpace(this, dimension);
    }
}
