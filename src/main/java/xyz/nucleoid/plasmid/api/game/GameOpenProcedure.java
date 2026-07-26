package xyz.nucleoid.plasmid.api.game;

import net.minecraft.core.Holder;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

/**
 * Holds the logic controlling how a {@link GameSpace} should be opened.
 *
 * @see GameOpenContext
 */
public interface GameOpenProcedure {
    static GameOpenProcedure withOverride(GameOpenProcedure procedure, Holder<GameConfig<?>> game) {
        return new GameOpenProcedure() {
            @Override
            public void apply(GameSpace context) {
                procedure.apply(context);
            }

            @Override
            public Holder<GameConfig<?>> configOverride() {
                return game;
            }
        };
    }

    void apply(GameSpace gameSpace);

    default Holder<GameConfig<?>> configOverride() {
        return null;
    }
}
