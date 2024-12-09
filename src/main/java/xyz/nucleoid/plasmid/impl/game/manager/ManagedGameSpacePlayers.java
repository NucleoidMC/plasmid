package xyz.nucleoid.plasmid.impl.game.manager;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.player.*;
import xyz.nucleoid.plasmid.impl.player.LocalJoinAcceptor;
import xyz.nucleoid.plasmid.impl.player.LocalJoinOffer;
import xyz.nucleoid.plasmid.api.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.impl.player.isolation.IsolatingPlayerTeleporter;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpacePlayers;
import xyz.nucleoid.plasmid.api.game.GameTexts;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public final class ManagedGameSpacePlayers implements GameSpacePlayers {
    private final ManagedGameSpace space;
    final MutablePlayerSet set;
    final MutablePlayerSet spectators;
    final MutablePlayerSet players;
    final IsolatingPlayerTeleporter teleporter;

    ManagedGameSpacePlayers(ManagedGameSpace space) {
        this.space = space;
        this.set = new MutablePlayerSet(space.getServer());
        this.spectators = new MutablePlayerSet(space.getServer());
        this.players = new MutablePlayerSet(space.getServer());
        this.teleporter = new IsolatingPlayerTeleporter(space.getServer());
    }

    @Override
    public GameResult simulateOffer(Collection<ServerPlayerEntity> players, JoinIntent intent) {
        if (players.stream().anyMatch(this.set::contains)) {
            return GameResult.error(GameTexts.Join.alreadyJoined());
        }

        var offer = new LocalJoinOffer(players, intent);

        return switch (this.space.offerPlayers(offer)) {
            case JoinOfferResult.Accept accept -> GameResult.ok();
            case JoinOfferResult.Reject reject -> GameResult.error(reject.reason());
            default -> GameResult.error(GameTexts.Join.genericError());
        };
    }

    @Override
    public GameResult offer(Collection<ServerPlayerEntity> players, JoinIntent intent) {
        var result = this.attemptOffer(players, intent);

        if (result.isError()) {
            this.attemptGarbageCollection();
        }

        return result;
    }

    private GameResult attemptOffer(Collection<ServerPlayerEntity> players, JoinIntent intent) {
        if (players.stream().anyMatch(this.set::contains)) {
            return GameResult.error(GameTexts.Join.alreadyJoined());
        }

        var offer = new LocalJoinOffer(players, intent);

        return switch (this.space.offerPlayers(offer)) {
            case JoinOfferResult.Accept accept -> this.accept(players, intent);
            case JoinOfferResult.Reject reject -> GameResult.error(reject.reason());
            default -> GameResult.error(GameTexts.Join.genericError());
        };
    }

    private GameResult accept(Collection<ServerPlayerEntity> players, JoinIntent intent) {
        var acceptor = new LocalJoinAcceptor(players, intent);

        switch (this.space.acceptPlayers(acceptor)) {
            case LocalJoinAcceptor.Teleport teleport -> {
                try {
                    var joiningSet = new MutablePlayerSet(this.space.getServer());
                    for (var player : players) {
                        this.teleporter.teleportIn(player, teleport::applyTeleport);
                        this.set.add(player);
                        this.byIntent(intent).add(player);
                        this.space.onAddPlayer(player);
                        joiningSet.add(player);
                    }
                    teleport.runCallbacks(joiningSet, intent);

                    return GameResult.ok();
                } catch (Throwable throwable) {
                    this.space.getLifecycle().onError(this.space, throwable, "handling LocalJoinAcceptor.Teleport");
                    return GameResult.error(GameTexts.Join.unexpectedError());
                }
            }
            default -> throw new IllegalStateException("Accept event must be handled");
        }
    }

    protected void attemptGarbageCollection() {
        if (this.set.isEmpty()) {
            this.space.close(GameCloseReason.GARBAGE_COLLECTED);
        }
    }

    @Override
    public boolean kick(ServerPlayerEntity player) {
        if (this.remove(player)) {
            this.teleporter.teleportOut(player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MutablePlayerSet byIntent(JoinIntent joinIntent) {
        return switch (joinIntent) {
            case PLAY -> this.players;
            case SPECTATE -> this.spectators;
        };
    }

    @Override
    public void modifyIntent(ServerPlayerEntity player, JoinIntent joinIntent) {
        this.spectators.remove(player);
        this.players.remove(player);
        this.byIntent(joinIntent).add(player);
    }

    @Override
    public PlayerSet spectators() {
        return this.spectators;
    }

    @Override
    public PlayerSet participants() {
        return this.players;
    }

    public boolean remove(ServerPlayerEntity player) {
        if (!this.set.contains(player)) {
            return false;
        }

        this.space.onPlayerRemove(player);

        this.set.remove(player);
        this.players.remove(player);
        this.spectators.remove(player);

        this.attemptGarbageCollection();

        return true;
    }

    void clear() {
        this.set.clear();
    }

    @Override
    public boolean contains(UUID id) {
        return this.set.contains(id);
    }

    @Override
    @Nullable
    public ServerPlayerEntity getEntity(UUID id) {
        return this.set.getEntity(id);
    }

    @Override
    public int size() {
        return this.set.size();
    }

    @Override
    public @NotNull Iterator<ServerPlayerEntity> iterator() {
        return this.set.iterator();
    }

    public IsolatingPlayerTeleporter getTeleporter() {
        return this.teleporter;
    }
}
