package xyz.nucleoid.plasmid.map.template;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.stream.Stream;

public final class MapTemplatePlacer {
    private final MapTemplate template;

    public MapTemplatePlacer(MapTemplate template) {
        this.template = template;
    }

    public void placeAt(ServerWorld world, BlockPos origin) {
        Long2ObjectMap<WorldChunk> chunkCache = this.collectChunks(world, origin, this.template.bounds);

        this.clearLighting(world, chunkCache);

        this.placeBlocks(origin, chunkCache);
        this.placeEntities(world, origin);
    }

    private Long2ObjectMap<WorldChunk> collectChunks(ServerWorld world, BlockPos origin, BlockBounds bounds) {
        LongSet chunkPositions = bounds.offset(origin).asChunks();
        LongIterator chunkIterator = chunkPositions.iterator();

        Long2ObjectMap<WorldChunk> chunks = new Long2ObjectOpenHashMap<>(chunkPositions.size());
        while (chunkIterator.hasNext()) {
            long chunkPos = chunkIterator.nextLong();
            int chunkX = ChunkPos.getPackedX(chunkPos);
            int chunkZ = ChunkPos.getPackedZ(chunkPos);

            chunks.put(chunkPos, world.getChunk(chunkX, chunkZ));
        }

        return chunks;
    }

    private void clearLighting(ServerWorld world, Long2ObjectMap<WorldChunk> chunkCache) {
        ServerLightingProvider lightingProvider = world.getChunkManager().getLightingProvider();

        // delete all lighting data so that it gets recomputed
        for (WorldChunk chunk : chunkCache.values()) {
            ChunkPos chunkPos = chunk.getPos();

            chunk.setLightOn(false);

            lightingProvider.setColumnEnabled(chunkPos, false);
            lightingProvider.setRetainData(chunkPos, false);

            for (int y = -1; y < 17; y++) {
                lightingProvider.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(chunkPos, y), null, true);
                lightingProvider.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(chunkPos, y), null, true);
            }

            for (int y = 0; y < 16; y++) {
                lightingProvider.setSectionStatus(ChunkSectionPos.from(chunkPos, y), true);
            }
        }
    }

    private void placeBlocks(BlockPos origin, Long2ObjectMap<WorldChunk> chunkCache) {
        MapTemplate template = this.template;
        BlockBounds bounds = template.getBounds();

        BlockPos.Mutable worldPos = new BlockPos.Mutable();

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        for (BlockPos templatePos : bounds) {
            worldPos.set(templatePos, originX, originY, originZ);

            BlockState state = template.getBlockState(templatePos);
            if (state.isAir()) {
                continue;
            }

            int chunkX = worldPos.getX() >> 4;
            int chunkZ = worldPos.getZ() >> 4;

            long chunkPos = ChunkPos.toLong(chunkX, chunkZ);
            WorldChunk chunk = chunkCache.get(chunkPos);

            CompoundTag blockEntity = template.getBlockEntityTag(templatePos, worldPos);
            if (blockEntity != null) {
                chunk.addPendingBlockEntityTag(blockEntity);
            }

            chunk.setBlockState(worldPos, state, false);
        }
    }

    private void placeEntities(ServerWorld world, BlockPos origin) {
        MapTemplate template = this.template;

        LongSet chunks = template.getBounds().asChunkSections();
        LongIterator chunkIterator = chunks.iterator();

        while (chunkIterator.hasNext()) {
            long chunkPos = chunkIterator.nextLong();
            int chunkX = ChunkSectionPos.unpackX(chunkPos);
            int chunkY = ChunkSectionPos.unpackY(chunkPos);
            int chunkZ = ChunkSectionPos.unpackZ(chunkPos);

            Stream<MapEntity> entities = template.getEntitiesInChunk(chunkX, chunkY, chunkZ);
            entities.forEach(mapEntity -> {
                mapEntity.createEntities(world, origin, world::spawnEntity);
            });
        }
    }
}
