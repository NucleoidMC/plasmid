package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.player.PlayerOps;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.Collection;

/**
 * Represents all {@link ServerPlayerEntity}s in this {@link GameSpace}. This provides utilities to operate on many
 * players through {@link PlayerOps}, as well as functionality to manage the players within the {@link GameSpace}.
 *
 * @see PlayerSet
 * @see PlayerOps
 *
 * @deprecated {@link ListedGameSpace} should be interacted with directly, or the underlying {@link PlayerSet}
 */
public interface GameSpacePlayers extends PlayerSet {
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
