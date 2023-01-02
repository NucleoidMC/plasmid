package xyz.nucleoid.plasmid.game;

import xyz.nucleoid.plasmid.game.config.GameConfig;

/**
 * Holds the logic controlling how a {@link GameSpace} should be opened.
 *
 * @see GameOpenContext
 */
public interface GameOpenProcedure {
    static GameOpenProcedure withOverride(GameOpenProcedure procedure, GameConfig<?> game) {
        return new GameOpenProcedure() {
            @Override
            public void apply(GameSpace context) {
                procedure.apply(context);
            }

            @Override
            public GameConfig<?> configOverride() {
                return game;
            }
        };
    }

    void apply(GameSpace gameSpace);

    default GameConfig<?> configOverride() {
        return null;
    }
}
