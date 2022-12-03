package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

public interface ListedGameSpace {
    /**
     * @return all metadata associated with this {@link ListedGameSpace}
     */
    ListedGameSpaceMetadata getMetadata();

    /**
     * Returns all {@link ServerPlayerEntity}s in this {@link GameSpace}.
     *
     * <p>{@link GameSpacePlayers#contains(ServerPlayerEntity)} can be used to check if a {@link ServerPlayerEntity} is in this {@link GameSpace} instead.
     *
     * @return a {@link PlayerSet} that contains all {@link ServerPlayerEntity}s in this {@link GameSpace}
     */
    GameSpacePlayers getPlayers();

    /**
     * @return {@code true} if this {@link ListedGameSpace} is closed, {@code false} otherwise
     */
    boolean isClosed();
}
