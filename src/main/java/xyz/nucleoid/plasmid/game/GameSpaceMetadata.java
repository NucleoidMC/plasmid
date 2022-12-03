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
        GameConfig<?> sourceConfig
) implements ListedGameSpaceMetadata {
    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public Identifier userId() {
        return this.userId;
    }

    @Override
    public GameConfig<?> sourceConfig() {
        return this.sourceConfig;
    }
}
