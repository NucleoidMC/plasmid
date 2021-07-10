package xyz.nucleoid.plasmid.game;

/**
 * Holds the logic controlling how a {@link GameSpace} should be opened.
 *
 * @see GameOpenContext
 */
public interface GameOpenProcedure {
    void apply(GameSpace gameSpace);
}
