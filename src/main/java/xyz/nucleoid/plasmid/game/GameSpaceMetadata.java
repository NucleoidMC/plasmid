package xyz.nucleoid.plasmid.game;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.util.UUID;

/**
 * Represents the static metadata to a {@link GameSpace} relating to how it should be referenced (by IDs) and which
 * game configuration is responsible for constructing it.
 */
public record GameSpaceMetadata(
        UUID id,
        Identifier userId,
        GameConfig<?> sourceConfig,
        GameConfig<?> originalSourceConfig
) {


    public GameSpaceMetadata(
            UUID id,
            Identifier userId,
            GameConfig<?> sourceConfig
    ) {
        this(id, userId, sourceConfig, sourceConfig);
    }
    /**
     * @return the globally unique ID for this {@link GameSpace}
     */
    @Override
    public UUID id() {
        return this.id;
    }

    /**
     * Returns the ID assigned to this {@link GameSpace} instance that can be referenced by players in commands.
     * This ID is not guaranteed to be unique over time, but only unique during the existence of this {@link GameSpace}!
     *
     * @return the user-referencable ID for this {@link GameSpace}
     */
    @Override
    public Identifier userId() {
        return this.userId;
    }

    /**
     * @return the {@link GameConfig} that was responsible for creating this {@link GameSpace}
     */
    @Override
    public GameConfig<?> sourceConfig() {
        return this.sourceConfig;
    }

    public boolean isSourceConfig(GameConfig<?> gameConfig) {
        return this.sourceConfig == gameConfig || this.originalSourceConfig == gameConfig;
    }
}
