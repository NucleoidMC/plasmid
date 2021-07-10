package xyz.nucleoid.plasmid.map.template;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class MapTemplatePlacer {
    private final MapTemplate template;

    public MapTemplatePlacer(MapTemplate template) {
        this.template = template;
    }

    public void placeAt(ServerWorld world, BlockPos origin) {
        var chunkCache = this.collectChunks(world, origin, this.template.bounds);

        this.clearLighting(world, chunkCache);

        this.placeBlocks(origin, chunkCache);
        this.placeEntities(world, origin);
    }

    private Long2ObjectMap<WorldChunk> collectChunks(ServerWorld world, BlockPos origin, BlockBounds bounds) {
        var chunkPositions = bounds.offset(origin).asChunks();
        var chunkIterator = chunkPositions.iterator();

        var chunks = new Long2ObjectOpenHashMap<WorldChunk>(chunkPositions.size());
        while (chunkIterator.hasNext()) {
            long chunkPos = chunkIterator.nextLong();
            int chunkX = ChunkPos.getPackedX(chunkPos);
            int chunkZ = ChunkPos.getPackedZ(chunkPos);

            chunks.put(chunkPos, world.getChunk(chunkX, chunkZ));
        }

        return chunks;
    }

    private void clearLighting(ServerWorld world, Long2ObjectMap<WorldChunk> chunkCache) {
        var lightingProvider = world.getChunkManager().getLightingProvider();

        // delete all lighting data so that it gets recomputed
        for (var chunk : chunkCache.values()) {
            var chunkPos = chunk.getPos();

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
        var template = this.template;
        var bounds = template.getBounds();

        var worldPos = new BlockPos.Mutable();

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        for (var templatePos : bounds) {
            worldPos.set(templatePos, originX, originY, originZ);

            BlockState state = template.getBlockState(templatePos);
            if (state.isAir()) {
                continue;
            }

            int chunkX = worldPos.getX() >> 4;
            int chunkZ = worldPos.getZ() >> 4;

            long chunkPos = ChunkPos.toLong(chunkX, chunkZ);
            var chunk = chunkCache.get(chunkPos);

            var blockEntity = template.getBlockEntityTag(templatePos, worldPos);
            if (blockEntity != null) {
                chunk.addPendingBlockEntityNbt(blockEntity);
            }

            chunk.setBlockState(worldPos, state, false);
        }
    }

    private void placeEntities(ServerWorld world, BlockPos origin) {
        var template = this.template;

        var chunks = template.getBounds().asChunkSections();
        var chunkIterator = chunks.iterator();

        while (chunkIterator.hasNext()) {
            long chunkPos = chunkIterator.nextLong();
            int chunkX = ChunkSectionPos.unpackX(chunkPos);
            int chunkY = ChunkSectionPos.unpackY(chunkPos);
            int chunkZ = ChunkSectionPos.unpackZ(chunkPos);

            var entities = template.getEntitiesInChunk(chunkX, chunkY, chunkZ);
            entities.forEach(mapEntity -> {
                mapEntity.createEntities(world, origin, world::spawnEntity);
            });
        }
    }
}
