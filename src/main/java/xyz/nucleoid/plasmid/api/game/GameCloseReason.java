package xyz.nucleoid.plasmid.api.game;

import xyz.nucleoid.plasmid.api.event.GameEvents;

/**
 * Represents a reason for a game to close.
 * 
 * <p>To close a game, see {@link GameSpace#close}.
 * <p>To listen for game closure, see {@link GameEvents#CLOSING}.
 */
public enum GameCloseReason {
    /**
     * Used when a game ends normally.
     */
    FINISHED,
    /**
     * Used when the server closes or a player manually runs a command to close the game.
     */
    CANCELED,
    /**
     * Used when the game space is unloaded or the game's last player leaves, making the game no longer useful.
     */
    GARBAGE_COLLECTED,
    /**
     * Used when an exception is thrown that requires the game to close.
     */
    ERRORED,
    /**
     * Used when an activity is swapped for another.
     */
    SWAPPED,
}
