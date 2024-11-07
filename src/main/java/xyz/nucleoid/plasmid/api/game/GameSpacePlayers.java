package xyz.nucleoid.plasmid.api.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.PlayerOps;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;

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
     * Simulates offer to join a player or group of players and returns whether they should be allowed into the game.
     * <p>
     * This logic is controlled through the active {@link GameActivity} through {@link GamePlayerEvents#OFFER}.
     *
     * @param players the group of players trying to join
     * @param intent the intent of the players trying to join, such as whether they want to participate or spectate
     * @return a {@link GameResult} describing whether these players can join this game, or an error if not
     * @see GameSpacePlayers#offer(Collection, JoinIntent)
     * @see GamePlayerJoiner
     */
    GameResult simulateOffer(Collection<ServerPlayerEntity> players, JoinIntent intent);

    /**
     * Offers a player or group of players to join this game. If accepted, they will be teleported into the game, and if not
     * an error {@link GameResult} will be returned.
     * <p>
     * This logic is controlled through the active {@link GameActivity} through {@link GamePlayerEvents#OFFER}.
     *
     * @param players the players trying to join
     * @param intent the intent of the players trying to join, such as whether they want to participate or spectate
     * @return a {@link GameResult} describing whether these players joined the game, or an error if not
     * @see GamePlayerEvents#OFFER
     * @see GamePlayerJoiner
     */
    GameResult offer(Collection<ServerPlayerEntity> players, JoinIntent intent);

    /**
     * Attempts to remove the given {@link ServerPlayerEntity} from this {@link GameSpace}.
     * When a player is removed, they will be teleported back to their former location prior to joining.
     *
     * @param player {@link ServerPlayerEntity} to remove from this {@link GameSpace}
     * @return whether the {@link ServerPlayerEntity} was successfully removed
     */
    boolean kick(ServerPlayerEntity player);

    /**
     * @return a {@link PlayerSet} of players who joined with intent to only spectate
     */
    PlayerSet spectators();

    /**
     * @return a {@link PlayerSet} of players who joined with intent to actively play
     */
    PlayerSet players();

    /**
     * @return a {@link PlayerSet} of players who joined with provided intent
     */
    PlayerSet byIntent(JoinIntent joinIntent);

    /**
     * Changes stored player's join intent to selected one, allowing to switch player's state.
     *
     * @param player {@link ServerPlayerEntity} to change intent of
     * @param joinIntent {@link JoinIntent} a new join intent
     */
    void modifyIntent(ServerPlayerEntity player, JoinIntent joinIntent);
}
