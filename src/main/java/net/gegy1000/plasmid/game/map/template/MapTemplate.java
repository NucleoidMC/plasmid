package net.gegy1000.plasmid.game.map.template;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.plasmid.Plasmid;
import net.gegy1000.plasmid.util.BlockBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

public final class MapTemplate {
    private static final Path MAP_ROOT = Paths.get(Plasmid.ID, "map");

    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    final Long2ObjectMap<MapChunk> chunks = new Long2ObjectOpenHashMap<>();
    final Long2ObjectMap<CompoundTag> blockEntities = new Long2ObjectOpenHashMap<>();
    final List<TemplateRegion> regions = new ArrayList<>();

    BlockBounds bounds = null;

    private MapTemplate() {
    }

    public static MapTemplate createEmpty() {
        return new MapTemplate();
    }

    public static CompletableFuture<MapTemplate> load(Identifier identifier) {
        return CompletableFuture.supplyAsync(() -> {
            Path path = getPathFor(identifier).resolve("map.nbt");

            try (InputStream input = Files.newInputStream(path)) {
                MapTemplate map = new MapTemplate();
                map.load(NbtIo.readCompressed(input));
                return map;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.method_27958());
    }

    private void load(CompoundTag root) {
        ListTag chunkList = root.getList("chunks", 10);
        for (int i = 0; i < chunkList.size(); i++) {
            CompoundTag chunkRoot = chunkList.getCompound(i);

            int[] posArray = chunkRoot.getIntArray("pos");
            if (posArray.length != 3) {
                Plasmid.LOGGER.warn("Invalid chunk pos key: {}", posArray);
                continue;
            }

            BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
            MapChunk chunk = MapChunk.deserialize(chunkRoot);

            this.chunks.put(pos.asLong(), chunk);
        }

        ListTag regionList = root.getList("regions", 10);
        for (int i = 0; i < regionList.size(); i++) {
            CompoundTag regionRoot = regionList.getCompound(i);
            this.regions.add(TemplateRegion.deserialize(regionRoot));
        }

        ListTag blockEntityList = root.getList("block_entities", 10);
        for (int i = 0; i < blockEntityList.size(); i++) {
            CompoundTag blockEntity = blockEntityList.getCompound(i);
            BlockPos pos = new BlockPos(
                    blockEntity.getInt("x"),
                    blockEntity.getInt("y"),
                    blockEntity.getInt("z")
            );
            this.blockEntities.put(pos.asLong(), blockEntity);
        }

        this.bounds = BlockBounds.deserialize(root.getCompound("bounds"));
    }

    public void save(Identifier identifier) throws IOException {
        CompoundTag root = new CompoundTag();

        ListTag chunkList = new ListTag();

        for (Long2ObjectMap.Entry<MapChunk> entry : Long2ObjectMaps.fastIterable(this.chunks)) {
            ChunkSectionPos pos = ChunkSectionPos.from(entry.getLongKey());
            MapChunk chunk = entry.getValue();

            CompoundTag chunkRoot = new CompoundTag();

            chunkRoot.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            chunk.serialize(chunkRoot);

            chunkList.add(chunkRoot);
        }

        root.put("chunks", chunkList);

        ListTag regionList = new ListTag();
        for (TemplateRegion region : this.regions) {
            regionList.add(region.serialize(new CompoundTag()));
        }
        root.put("regions", regionList);

        ListTag blockEntityList = new ListTag();
        blockEntityList.addAll(this.blockEntities.values());
        root.put("block_entities", blockEntityList);

        root.put("bounds", this.bounds.serialize(new CompoundTag()));

        Path parent = getPathFor(identifier);
        Files.createDirectories(parent);

        Path path = parent.resolve("map.nbt");

        try (OutputStream output = Files.newOutputStream(path)) {
            NbtIo.writeCompressed(root, output);
        }
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        MapChunk chunk = this.chunks.computeIfAbsent(chunkPos(pos), p -> new MapChunk());
        chunk.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
    }

    public BlockState getBlockState(BlockPos pos) {
        MapChunk chunk = this.chunks.get(chunkPos(pos));
        if (chunk != null) {
            return chunk.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        }
        return AIR;
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

    private static Path getPathFor(Identifier identifier) {
        return MAP_ROOT.resolve(identifier.getNamespace()).resolve(identifier.getPath());
    }

    public BlockBounds getBounds() {
        if (this.bounds == null) {
            this.bounds = this.computeBounds();
        }
        return this.bounds;
    }

    private BlockBounds computeBounds() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Long2ObjectMap.Entry<MapChunk> entry : Long2ObjectMaps.fastIterable(this.chunks)) {
            ChunkSectionPos chunkPos = ChunkSectionPos.from(entry.getLongKey());

            int minChunkX = chunkPos.getMinX();
            int minChunkY = chunkPos.getMinY();
            int minChunkZ = chunkPos.getMinZ();

            int maxChunkX = chunkPos.getMaxX();
            int maxChunkY = chunkPos.getMaxY();
            int maxChunkZ = chunkPos.getMaxZ();

            if (minChunkX < minX) minX = minChunkX;
            if (minChunkY < minY) minY = minChunkY;
            if (minChunkZ < minZ) minZ = minChunkZ;

            if (maxChunkX > maxX) maxX = maxChunkX;
            if (maxChunkY > maxY) maxY = maxChunkY;
            if (maxChunkZ > maxZ) maxZ = maxChunkZ;
        }

        return new BlockBounds(
                new BlockPos(minX, minY, minZ),
                new BlockPos(maxX, maxY, maxZ)
        );
    }

    private static class MapChunk {
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

        public static MapChunk deserialize(CompoundTag tag) {
            MapChunk chunk = new MapChunk();
            chunk.container.read(tag.getList("palette", 10), tag.getLongArray("block_states"));
            return chunk;
        }
    }
}
