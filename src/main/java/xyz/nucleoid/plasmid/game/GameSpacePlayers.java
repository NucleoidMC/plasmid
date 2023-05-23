package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOps;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Represents all {@link ServerPlayerEntity}s in this {@link GameSpace}. This provides utilities to operate on many
 * players through {@link PlayerOps}, as well as functionality to manage the players within the {@link GameSpace}.
 *
 * @see PlayerSet
 * @see PlayerOps
 */
public interface GameSpacePlayers extends PlayerSet {
    /**
     * Screens a group of players and returns whether the collective group should be allowed into the game.
     * <p>
     * This logic is controlled through the active {@link GameActivity} through {@link GamePlayerEvents#SCREEN_JOINS}.
     *
     * @param players the group of players trying to join
     * @return a {@link GameResult} describing whether this group can join this game, or an error if not
     * @see GamePlayerEvents#SCREEN_JOINS
     * @see GameSpacePlayers#offer(OfferContext)
     * @see xyz.nucleoid.plasmid.game.player.GamePlayerJoiner
     */
    GameResult screenJoins(Collection<ServerPlayerEntity> players);

    /**
     * Offers an individual player to join this game. If accepted, they will be teleported into the game, and if not
     * an error {@link GameResult} will be returned.
     * <p>
     * This logic is controlled through the active {@link GameActivity} through {@link GamePlayerEvents#OFFER}.
     *
     * @param player the player trying to join
     * @return a {@link GameResult} describing whether this player joined the game, or an error if not
     * @see GamePlayerEvents#OFFER
     * @see xyz.nucleoid.plasmid.game.player.GamePlayerJoiner
     */
    GameResult offer(OfferContext player);

    /**
     * Attempts to remove the given {@link ServerPlayerEntity} from this {@link GameSpace}.
     * When a player is removed, his resetter will be called.
     *
     * @param player {@link ServerPlayerEntity} to remove from this {@link GameSpace}
     * @return whether the {@link ServerPlayerEntity} was successfully removed
     */
    boolean kick(ServerPlayerEntity player);


    /**
     * Represents a context for a player trying to join a {@link GameSpace}.
     * <p>
     * This is used to provide additional information to the {@link GameActivity} when a player is trying to join.
     * <p>
     * On the default implementation, it's created through {@link xyz.nucleoid.plasmid.game.player.GamePlayerJoiner#getContext(ServerPlayerEntity, GameSpace)}.
     */
    record OfferContext(ServerPlayerEntity player, Runnable onApply, boolean sendFirstJoinPacket, BiConsumer<ServerPlayerEntity, GameSpace> leaveHandler) {}


}
