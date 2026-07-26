package xyz.nucleoid.plasmid.api.game.level;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Iterator;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

/**
 * Represents all temporary {@link ServerLevel} instances attached to this {@link GameSpace}.
 */
public interface GameSpaceLevels extends Iterable<ServerLevel> {
    /**
     * Creates and adds a temporary level to be associated with this {@link GameSpace}.
     * When the game is closed, the level will be deleted.
     *
     * @param levelConfig a config describing how the new level should be created
     * @return the created level instance
     * @see RuntimeLevelConfig
     */
    ServerLevel add(RuntimeLevelConfig levelConfig);


    /**
     * Creates (or loads) and adds a persistent level to be associated with this {@link GameSpace}.
     * When the game is closed, the level will be unloaded, with all chunks saved.
     * If level using this id is already loaded, this method will throw an exception.
     *
     * @param worldConfig a config describing how the new level should be created and how it should behave
     * @return the created level instance
     * @see RuntimeLevelConfig
     */
    @ApiStatus.Experimental
    ServerLevel addPersistent(Identifier identifier, RuntimeLevelConfig worldConfig);


    /**
     * Removes and deletes a temporary level that is associated with this {@link GameSpace}.
     * The passed level must have been created through {@link GameSpaceLevels#add(RuntimeLevelConfig)}.
     *
     * @param level the level instance to delete
     * @see GameSpaceLevels#add(RuntimeLevelConfig)
     */
    boolean remove(ServerLevel level);

    @NotNull
    @Override
    Iterator<ServerLevel> iterator();
}
