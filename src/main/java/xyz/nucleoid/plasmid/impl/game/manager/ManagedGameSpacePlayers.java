package xyz.nucleoid.plasmid.impl.game.manager;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.player.*;
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
    final IsolatingPlayerTeleporter teleporter;

    ManagedGameSpacePlayers(ManagedGameSpace space) {
        this.space = space;
        this.set = new MutablePlayerSet(space.getServer());
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
                        this.space.onAddPlayer(player);
                        joiningSet.add(player);
                    }
                    teleport.runCallbacks(joiningSet);

                    return GameResult.ok();
                } catch (Throwable throwable) {
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

    public boolean remove(ServerPlayerEntity player) {
        if (!this.set.contains(player)) {
            return false;
        }

        this.space.onPlayerRemove(player);

        this.set.remove(player);

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
    public Iterator<ServerPlayerEntity> iterator() {
        return this.set.iterator();
    }

    public IsolatingPlayerTeleporter getTeleporter() {
        return this.teleporter;
    }
}
