package xyz.nucleoid.plasmid.map.template;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.util.BlockBounds;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents a map template.
 * <p>
 * A map template stores serialized chunks, block entities, entities, the bounds, the biome, and regions.
 * <p>
 * It can be loaded from resources with {@link MapTemplateSerializer#loadFromResource(Identifier)},
 * and used for generation with {@link TemplateChunkGenerator}
 */
public final class MapTemplate {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    final Long2ObjectMap<MapChunk> chunks = new Long2ObjectOpenHashMap<>();
    final Long2ObjectMap<CompoundTag> blockEntities = new Long2ObjectOpenHashMap<>();

    RegistryKey<Biome> biome = BiomeKeys.THE_VOID;

    BlockBounds bounds = null;

    MapTemplateMetadata metadata = new MapTemplateMetadata();

    private MapTemplate() {
    }

    public static MapTemplate createEmpty() {
        return new MapTemplate();
    }

    /**
     * Sets the biome key of the map template.
     *
     * @param biome The biome key.
     */
    public void setBiome(RegistryKey<Biome> biome) {
        this.biome = biome;
    }

    /**
     * Returns the biome key of the map template.
     *
     * @return The biome key.
     */
    public RegistryKey<Biome> getBiome() {
        return this.biome;
    }

    /**
     * Returns the non-world data of this MapTemplate that can be used to control additional game logic, but has no
     * impact in what blocks or entities are placed in the world. This includes regions and arbitrary attached data.
     *
     * @return the map template metadata for this map.
     */
    public MapTemplateMetadata getMetadata() {
        return this.metadata;
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        MapChunk chunk = this.getOrCreateChunk(chunkPos(pos));
        chunk.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);

        if (state.getBlock().hasBlockEntity()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", "DUMMY");
            this.blockEntities.put(pos.asLong(), tag);
        }
    }

    public void setBlockEntity(BlockPos pos, @Nullable BlockEntity entity) {
        if (entity != null) {
            CompoundTag entityTag = entity.toTag(new CompoundTag());
            entityTag.putInt("x", pos.getX());
            entityTag.putInt("y", pos.getY());
            entityTag.putInt("z", pos.getZ());

            this.blockEntities.put(pos.asLong(), entityTag);
        } else {
            this.blockEntities.remove(pos.asLong());
        }
    }

    public BlockState getBlockState(BlockPos pos) {
        MapChunk chunk = this.chunks.get(chunkPos(pos));
        if (chunk != null) {
            return chunk.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        }
        return AIR;
    }

    @Nullable
    public CompoundTag getBlockEntityTag(BlockPos localPos) {
        CompoundTag tag = this.blockEntities.get(localPos.asLong());
        return tag != null ? tag.copy() : null;
    }

    @Nullable
    public CompoundTag getBlockEntityTag(BlockPos localPos, BlockPos worldPos) {
        CompoundTag tag = this.getBlockEntityTag(localPos);
        if (tag != null) {
            tag.putInt("x", worldPos.getX());
            tag.putInt("y", worldPos.getY());
            tag.putInt("z", worldPos.getZ());
            return tag;
        }
        return null;
    }

    /**
     * Adds an entity to the map template.
     * <p>
     * The position of the entity must be relative to the map template.
     *
     * @param entity The entity to add.
     * @param pos The entity position relatives to the map.
     */
    public void addEntity(Entity entity, Vec3d pos) {
        this.getOrCreateChunk(chunkPos(pos)).addEntity(entity, pos);
    }

    /**
     * Returns a stream of serialized entities from a chunk.
     *
     * @param chunkX The chunk X-coordinate.
     * @param chunkY The chunk Y-coordinate.
     * @param chunkZ The chunk Z-coordinate.
     * @return The stream of entities.
     */
    public Stream<MapEntity> getEntitiesInChunk(int chunkX, int chunkY, int chunkZ) {
        MapChunk chunk = this.chunks.get(chunkPos(chunkX, chunkY, chunkZ));
        return chunk != null ? chunk.getEntities().stream() : Stream.empty();
    }

    // TODO: store / lookup more efficiently?
    public int getTopY(int x, int z, Heightmap.Type heightmap) {
        Predicate<BlockState> predicate = heightmap.getBlockPredicate();

        BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, 0, z);
        for (int y = 255; y >= 0; y--) {
            mutablePos.setY(y);

            BlockState state = this.getBlockState(mutablePos);
            if (predicate.test(state)) {
                return y;
            }
        }

        return 0;
    }

    public BlockPos getTopPos(int x, int z, Heightmap.Type heightmap) {
        int y = this.getTopY(x, z, heightmap);
        return new BlockPos(x, y, z);
    }

    public boolean containsBlock(BlockPos pos) {
        return this.getBlockState(pos) != AIR;
    }

    @Nonnull
    private MapChunk getOrCreateChunk(long pos) {
        MapChunk chunk = this.chunks.get(pos);
        if (chunk == null) {
            this.chunks.put(pos, chunk = new MapChunk(ChunkSectionPos.from(pos)));
        }
        return chunk;
    }

    public void setBounds(BlockBounds bounds) {
        this.bounds = bounds;
    }

    public BlockBounds getBounds() {
        if (this.bounds == null) {
            this.bounds = this.computeBounds();
        }
        return this.bounds;
    }

    private BlockBounds computeBounds() {
        int minChunkX = Integer.MAX_VALUE;
        int minChunkY = Integer.MAX_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int maxChunkY = Integer.MIN_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;

        for (Long2ObjectMap.Entry<MapChunk> entry : Long2ObjectMaps.fastIterable(this.chunks)) {
            long chunkPos = entry.getLongKey();
            int chunkX = ChunkSectionPos.unpackX(chunkPos);
            int chunkY = ChunkSectionPos.unpackY(chunkPos);
            int chunkZ = ChunkSectionPos.unpackZ(chunkPos);

            if (chunkX < minChunkX) minChunkX = chunkX;
            if (chunkY < minChunkY) minChunkY = chunkY;
            if (chunkZ < minChunkZ) minChunkZ = chunkZ;

            if (chunkX > maxChunkX) maxChunkX = chunkX;
            if (chunkY > maxChunkY) maxChunkY = chunkY;
            if (chunkZ > maxChunkZ) maxChunkZ = chunkZ;
        }

        return new BlockBounds(
                new BlockPos(minChunkX << 4, minChunkY << 4, minChunkZ << 4),
                new BlockPos((maxChunkX << 4) + 15, (maxChunkY << 4) + 15, (maxChunkZ << 4) + 15)
        );
    }

    static long chunkPos(BlockPos pos) {
        return chunkPos(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    static long chunkPos(Vec3d pos) {
        return chunkPos(MathHelper.floor(pos.getX()) >> 4, MathHelper.floor(pos.getY()) >> 4, MathHelper.floor(pos.getZ()) >> 4);
    }

    static long chunkPos(int x, int y, int z) {
        return ChunkSectionPos.asLong(x, y, z);
    }
}
