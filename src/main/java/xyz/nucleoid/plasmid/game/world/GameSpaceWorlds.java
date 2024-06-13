package xyz.nucleoid.plasmid.game.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameSpace;

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
     * Regenerates a temporary world associated with this {@link GameSpace} with the given generator. This will
     * reset all blocks and all entities (other than players) in the world.
     * <p>
     * The parameter `chunksToDrop` should contain all chunks which have been generated and are nonempty. Otherwise,
     * unexpected behaviour (such as incorrect lighting or generation) may be encountered.
     *
     * @param world the world to regenerate
     * @param generator the chunk generator to regenerate it with
     * @param chunksToDrop the list of chunks to regenerate. This should consist of the world's nonempty, generated
     * chunks.
     */
    void regenerate(ServerWorld world, ChunkGenerator generator, LongSet chunksToDrop);

    /**
     * Regenerates a temporary world associated with this {@link GameSpace} with the given generator. This will
     * reset all blocks and all entities (other than players) in the world.
     * <p>
     * The parameter `worldBounds` should contain all chunks which have been generated and are nonempty. For instance,
     * when using an {@link xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator}, providing the union of
     * the old and new {@link xyz.nucleoid.map_templates.MapTemplate}s's bounds is sufficient.
     *
     * @param world the world to regenerate
     * @param generator the chunk generator to regenerate it with
     * @param worldBounds the bounds of the world to regenerate
     * chunks.
     */
    default void regenerate(ServerWorld world, ChunkGenerator generator, BlockBounds worldBounds) {
        this.regenerate(world, generator, worldBounds.asChunks());
    }

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
