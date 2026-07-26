package xyz.nucleoid.plasmid.api.game;

import xyz.nucleoid.plasmid.api.game.config.GameConfig;

import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

/**
 * Represents the static metadata to a {@link GameSpace} relating to how it should be referenced (by IDs) and which
 * game configuration is responsible for constructing it.
 */
public record GameSpaceMetadata(
        UUID id,
        Identifier userId,
        Holder<GameConfig<?>> sourceConfig,
        Holder<GameConfig<?>> originalSourceConfig
) {
    public GameSpaceMetadata(
            UUID id,
            Identifier userId,
            Holder<GameConfig<?>> sourceConfig
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
    public Holder<GameConfig<?>> sourceConfig() {
        return this.sourceConfig;
    }

    public boolean isSourceConfig(Holder<GameConfig<?>> gameConfig) {
        return this.sourceConfig.equals(gameConfig) || this.originalSourceConfig.equals(gameConfig);
    }
}
