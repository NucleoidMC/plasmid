package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.player.PlayerOps;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.Collection;

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
     * @see GameSpacePlayers#offer(ServerPlayerEntity)
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
    GameResult offer(ServerPlayerEntity player);

    /**
     * Attempts to remove the given {@link ServerPlayerEntity} from this {@link GameSpace}.
     * When a player is removed, they will be teleported back to their former location prior to joining.
     *
     * @param player {@link ServerPlayerEntity} to remove from this {@link GameSpace}
     * @return whether the {@link ServerPlayerEntity} was successfully removed
     */
    boolean kick(ServerPlayerEntity player);

    /**
     * Protein Utilities, this methods shouldn't be called by another mod !
     *
     * @param player {@link ServerPlayerEntity} to get the corresponding {@link PlayerOfferResult}
     * @return {@link PlayerOfferResult}
     */

    PlayerOfferResult getManualAcceptFor(ServerPlayerEntity player);
    void manualAdd(ServerPlayerEntity player);
}
