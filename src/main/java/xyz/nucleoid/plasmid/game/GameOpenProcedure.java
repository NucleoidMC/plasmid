package xyz.nucleoid.plasmid.game;

import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.game.config.GameConfig;

/**
 * Holds the logic controlling how a {@link GameSpace} should be opened.
 *
 * @see GameOpenContext
 */
public interface GameOpenProcedure {
    static GameOpenProcedure withOverride(GameOpenProcedure procedure, RegistryEntry<GameConfig<?>> game) {
        return new GameOpenProcedure() {
            @Override
            public void apply(GameSpace context) {
                procedure.apply(context);
            }

            @Override
            public RegistryEntry<GameConfig<?>> configOverride() {
                return game;
            }
        };
    }

    void apply(GameSpace gameSpace);

    default RegistryEntry<GameConfig<?>> configOverride() {
        return null;
    }
}
