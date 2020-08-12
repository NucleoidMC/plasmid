package xyz.nucleoid.plasmid.game.map.template;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class MapTemplate {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();
    final Long2ObjectMap<CompoundTag> blockEntities = new Long2ObjectOpenHashMap<>();
    final List<TemplateRegion> regions = new ArrayList<>();

    Biome biome = Biomes.THE_VOID;

    BlockBounds bounds = null;

    private MapTemplate() {
    }

    public static MapTemplate createEmpty() {
        return new MapTemplate();
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    public Biome getBiome() {
        return this.biome;
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        Chunk chunk = this.chunks.computeIfAbsent(chunkPos(pos), p -> new Chunk());
        chunk.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
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

    public void addRegion(String marker, BlockBounds bounds) {
        this.regions.add(new TemplateRegion(marker, bounds));
    }

    public void addRegion(TemplateRegion region) {
        this.regions.add(region);
    }

    public BlockState getBlockState(BlockPos pos) {
        Chunk chunk = this.chunks.get(chunkPos(pos));
        if (chunk != null) {
            return chunk.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        }
        return AIR;
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

    public Stream<BlockBounds> getRegions(String marker) {
        return this.regions.stream()
                .filter(region -> region.getMarker().equals(marker))
                .map(TemplateRegion::getBounds);
    }

    @Nullable
    public BlockBounds getFirstRegion(String marker) {
        return this.getRegions(marker).findFirst().orElse(null);
    }

    public boolean containsBlock(BlockPos pos) {
        return this.getBlockState(pos) != AIR;
    }

    private static long chunkPos(BlockPos pos) {
        return ChunkSectionPos.asLong(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
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

        for (Long2ObjectMap.Entry<Chunk> entry : Long2ObjectMaps.fastIterable(this.chunks)) {
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

    static class Chunk {
        private static final Palette<BlockState> PALETTE = new IdListPalette<>(Block.STATE_IDS, Blocks.AIR.getDefaultState());

        private final PalettedContainer<BlockState> container = new PalettedContainer<>(
                PALETTE, Block.STATE_IDS,
                NbtHelper::toBlockState, NbtHelper::fromBlockState,
                Blocks.AIR.getDefaultState()
        );

        public void set(int x, int y, int z, BlockState state) {
            this.container.set(x, y, z, state);
        }

        public BlockState get(int x, int y, int z) {
            return this.container.get(x, y, z);
        }

        public void serialize(CompoundTag tag) {
            this.container.write(tag, "palette", "block_states");
        }

        public static Chunk deserialize(CompoundTag tag) {
            Chunk chunk = new Chunk();
            chunk.container.read(tag.getList("palette", 10), tag.getLongArray("block_states"));
            return chunk;
        }
    }
}
