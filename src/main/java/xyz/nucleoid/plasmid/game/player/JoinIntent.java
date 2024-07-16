package xyz.nucleoid.plasmid.game.player;

import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

/**
 * Represents the "intention" of a player or group of players joining a {@link GameSpace}.
 * It is up to the game implementation to respect this intent in the way that is appropriate for their game. This may be
 * accomplished by handling the {@link GamePlayerEvents#SCREEN_JOINS 'Screen Joins'} and
 * {@link GamePlayerEvents#OFFER 'Join Offer'} events.
 */
public enum JoinIntent {
    /**
     * The player has no particular intention. Generally, this should be considered as a preference to participate.
     */
    ANY,
    /**
     * The player intends to join the game to participate. If they cannot be joined as a participant, they should not
     * be allowed to join.
     */
    PLAY,
    /**
     * The player intends to join the game to spectate. Unless the game does not support spectators, this player should
     * generally always be accepted.
     */
    SPECTATE,
    ;

    /**
     * @return {@code true} if the player may join as a participant under any circumstances
     */
    public boolean canPlay() {
        return this != SPECTATE;
    }

    /**
     * @return {@code true} if the player may join as a spectator under any circumstances
     */
    public boolean canSpectate() {
        return this != PLAY;
    }
}
