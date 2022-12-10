package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.player.PlayerOps;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * Represents all {@link ServerPlayerEntity}s in this {@link GameSpace}. This provides utilities to operate on many
 * players through {@link PlayerOps}, as well as functionality to manage the players within the {@link GameSpace}.
 *
 * @see PlayerSet
 * @see PlayerOps
 *
 * @deprecated {@link ListedGameSpace} should be interacted with directly, or the underlying {@link PlayerSet}
 */
@Deprecated(forRemoval = true)
public interface GameSpacePlayers extends PlayerSet {
    static GameSpacePlayers of(ListedGameSpace gameSpace, PlayerSet playerSet) {
        return new GameSpacePlayers() {
            @Override
            public GameResult screenJoins(Collection<ServerPlayerEntity> players) {
                return gameSpace.screenJoins(players);
            }

            @Override
            public GameResult offer(ServerPlayerEntity player) {
                return gameSpace.offer(player);
            }

            @Override
            public boolean kick(ServerPlayerEntity player) {
                return gameSpace.kick(player);
            }

            @Override
            public boolean contains(UUID id) {
                return playerSet.contains(id);
            }

            @Override
            @Nullable
            public ServerPlayerEntity getEntity(UUID id) {
                return playerSet.getEntity(id);
            }

            @Override
            public int size() {
                return playerSet.size();
            }

            @Override
            public Iterator<ServerPlayerEntity> iterator() {
                return playerSet.iterator();
            }
        };
    }

    /**
     * @deprecated use {@link ListedGameSpace#screenJoins(Collection)}
     */
    @Deprecated(forRemoval = true)
    GameResult screenJoins(Collection<ServerPlayerEntity> players);

    /**
     * @deprecated use {@link ListedGameSpace#offer(ServerPlayerEntity)}
     */
    @Deprecated(forRemoval = true)
    GameResult offer(ServerPlayerEntity player);

    /**
     * @deprecated use {@link ListedGameSpace#kick(ServerPlayerEntity)}
     */
    @Deprecated(forRemoval = true)
    boolean kick(ServerPlayerEntity player);
}
