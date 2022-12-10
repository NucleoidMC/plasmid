package xyz.nucleoid.plasmid.game;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.ListedGameConfig;

import java.util.UUID;

/**
 * Represents the static metadata to a {@link ListedGameSpace} relating to how it should be referenced (by IDs) and which
 * game configuration is responsible for constructing it.
 */
public interface ListedGameSpaceMetadata {
    /**
     * @return the globally unique ID for this {@link ListedGameSpace}
     */
    UUID id();

    /**
     * Returns the ID assigned to this {@link ListedGameSpace} instance that can be referenced by players in commands.
     * This ID is not guaranteed to be unique over time, but only unique during the existence of this {@link ListedGameSpace}!
     *
     * @return the user-referencable ID for this {@link ListedGameSpace}
     */
    Identifier userId();

    /**
     * @return the {@link ListedGameConfig} that was responsible for creating this {@link ListedGameSpace}
     */
    ListedGameConfig sourceConfig();
}
