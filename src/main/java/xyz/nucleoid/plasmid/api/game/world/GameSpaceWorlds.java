package xyz.nucleoid.plasmid.api.game.world;

import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Iterator;

/**
 * Represents all temporary {@link ServerWorld} instances attached to this {@link GameSpace}.
 */
public interface GameSpaceWorlds extends Iterable<ServerWorld> {
    /**
     * Creates and adds a temporary world to be associated with this {@link GameSpace}.
     * When the game is closed, the world will be deleted.
     *
     * @param worldConfig a config describing how the new world should be created
     * @return the created world instance
     * @see RuntimeWorldConfig
     */
    ServerWorld add(RuntimeWorldConfig worldConfig);

    /**
     * Removes and deletes a temporary world that is associated with this {@link GameSpace}.
     * The passed world must have been created through {@link GameSpaceWorlds#add(RuntimeWorldConfig)}.
     *
     * @param world the world instance to delete
     * @see GameSpaceWorlds#add(RuntimeWorldConfig)
     */
    boolean remove(ServerWorld world);

    @NotNull
    @Override
    Iterator<ServerWorld> iterator();
}
