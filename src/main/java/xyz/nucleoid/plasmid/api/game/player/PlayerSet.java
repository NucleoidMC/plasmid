package xyz.nucleoid.plasmid.api.game.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.impl.player.EmptyPlayerSet;
import xyz.nucleoid.plasmid.impl.player.ServerPlayerSet;
import xyz.nucleoid.plasmid.impl.player.ServerLevelPlayerSet;

import java.util.Iterator;
import java.util.UUID;

/**
 * Represents a set of {@link ServerPlayer} on a server. These players are not guaranteed to be currently online,
 * but all functionality will operate only on currently online players.
 * <p>
 * Can be iterated, and additionally implements {@link PlayerOps} which allows for quickly applying various operations
 * to all players within the set such as sending a message.
 *
 * @see MutablePlayerSet
 */
public interface PlayerSet extends PlayerIterable {
    PlayerSet EMPTY = EmptyPlayerSet.INSTANCE;

    static PlayerSet ofServer(MinecraftServer server) {
        return new ServerPlayerSet(server.getPlayerList());
    }

    static PlayerSet ofLevel(ServerLevel level) {
        return new ServerLevelPlayerSet(level);
    }

    /**
     * Queries whether this {@link PlayerSet} contains the given player {@link UUID}.
     * This will return {@code true} for players that are included in the {@link PlayerSet} even if they are not online.
     *
     * @param id the player uuid to query
     * @return {@code true} if this player {@link UUID} is contained within this {@link PlayerSet}
     */
    boolean contains(UUID id);

    /**
     * Queries whether this {@link PlayerSet} contains the given {@link PlayerRef}.
     * This will return {@code true} for players that are included in the {@link PlayerSet} even if they are not online.
     *
     * @param ref the {@link PlayerRef} to query
     * @return {@code true} if this {@link PlayerRef} is contained within this {@link PlayerSet}
     */
    default boolean contains(PlayerRef ref) {
        return this.contains(ref.id());
    }

    /**
     * Queries whether this {@link PlayerSet} contains the given {@link ServerPlayer}.
     *
     * @param player the {@link ServerPlayer} to query
     * @return {@code true} if this {@link ServerPlayer} is contained within this {@link PlayerSet}
     */
    default boolean contains(ServerPlayer player) {
        return this.contains(player.getUUID());
    }

    /**
     * Looks up a corresponding online {@link ServerPlayer} that is contained within this {@link PlayerSet}
     * given a player {@link UUID}.
     *
     * @param id the id to look up in this set
     * @return the corresponding online {@link ServerPlayer}, or {@code null} if not contained or offline
     */
    @Nullable
    ServerPlayer getEntity(UUID id);

    /**
     * Returns the number of players contained within this {@link PlayerSet}, including offline players.
     *
     * @return the number of players in this {@link PlayerSet}
     */
    int size();

    /**
     * Returns whether this {@link PlayerSet} is empty (including offline players).
     *
     * @return {@code true} if this {@link PlayerSet} is empty
     */
    default boolean isEmpty() {
        return this.size() <= 0;
    }

    /**
     * Creates a mutable copy of this {@link PlayerSet}.
     *
     * @param server the {@link MinecraftServer} instance that these players exist within
     * @return a mutable copy of this {@link PlayerSet}
     */
    default MutablePlayerSet copy(MinecraftServer server) {
        var copy = new MutablePlayerSet(server);
        this.forEach(copy::add);
        return copy;
    }

    /**
     * @return an iterator over the online {@link ServerPlayer} within this {@link PlayerSet}
     */
    @Override
    @NotNull
    Iterator<ServerPlayer> iterator();
}
