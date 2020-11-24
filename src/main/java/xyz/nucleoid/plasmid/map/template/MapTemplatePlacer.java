package xyz.nucleoid.plasmid.map.template;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.stream.Stream;

public final class MapTemplatePlacer {
    private final MapTemplate template;

    public MapTemplatePlacer(MapTemplate template) {
        this.template = template;
    }

    public void placeAt(ServerWorld world, BlockPos origin) {
        MapTemplate template = this.template;
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        Long2ObjectMap<WorldChunk> chunkCache = new Long2ObjectOpenHashMap<>();

        BlockPos.Mutable worldPos = new BlockPos.Mutable();

        BlockBounds bounds = template.getBounds();

        for (BlockPos templatePos : bounds) {
            int chunkX = templatePos.getX() >> 4;
            int chunkZ = templatePos.getZ() >> 4;

            long chunkPos = ChunkPos.toLong(chunkX, chunkZ);
            WorldChunk chunk = chunkCache.get(chunkPos);
            if (chunk == null) {
                chunkCache.put(chunkPos, chunk = world.getChunk(chunkX, chunkZ));
            }

            worldPos.set(templatePos, originX, originY, originZ);

            BlockState state = template.getBlockState(templatePos);

            CompoundTag blockEntity = template.getBlockEntityTag(templatePos, worldPos);
            if (blockEntity != null) {
                chunk.addPendingBlockEntityTag(blockEntity);
            }

            chunk.setBlockState(worldPos, state, false);
        }

        LongSet chunks = bounds.asChunkSections();
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
