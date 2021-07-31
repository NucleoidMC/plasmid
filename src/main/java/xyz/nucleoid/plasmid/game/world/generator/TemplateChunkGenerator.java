package xyz.nucleoid.plasmid.game.world.generator;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapChunk;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TemplateChunkGenerator extends GameChunkGenerator {
    private final MapTemplate template;
    private final BlockBounds worldBounds;

    public TemplateChunkGenerator(MinecraftServer server, MapTemplate template) {
        super(createBiomeSource(server, template.getBiome()), new StructuresConfig(Optional.empty(), Collections.emptyMap()));

        this.template = template;
        this.worldBounds = template.getBounds();

        if (this.worldBounds.min().getY() < 0 || this.worldBounds.max().getY() > 255) {
            throw new IllegalArgumentException("map template does not fit in world height range [0; 256]");
        }
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registryManager, StructureAccessor accessor, Chunk chunk, StructureManager manager, long seed) {
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk) {
        var chunkPos = chunk.getPos();

        var chunkBounds = BlockBounds.ofChunk(chunk);
        if (!this.worldBounds.intersects(chunkBounds)) {
            return CompletableFuture.completedFuture(chunk);
        }

        return CompletableFuture.supplyAsync(() -> {
            var protoChunk = (ProtoChunk) chunk;
            var mutablePos = new BlockPos.Mutable();

            int minWorldX = chunkPos.getStartX();
            int minWorldZ = chunkPos.getStartZ();

            int minSectionY = this.worldBounds.min().getY() >> 4;
            int maxSectionY = this.worldBounds.max().getY() >> 4;

            for (int sectionY = maxSectionY; sectionY >= minSectionY; sectionY--) {
                long sectionPos = ChunkSectionPos.asLong(chunkPos.x, sectionY, chunkPos.z);

                var templateChunk = this.template.getChunk(sectionPos);
                if (templateChunk == null) {
                    continue;
                }

                var section = protoChunk.getSection(sectionY);
                section.lock();

                try {
                    int minWorldY = sectionY << 4;
                    this.addSection(minWorldX, minWorldY, minWorldZ, mutablePos, protoChunk, section, templateChunk);
                } finally {
                    section.unlock();
                }
            }

            return chunk;
        }, executor);
    }

    private void addSection(int minWorldX, int minWorldY, int minWorldZ, BlockPos.Mutable templatePos, ProtoChunk chunk, ChunkSection section, MapChunk templateChunk) {
        var oceanFloor = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        var worldSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    var state = templateChunk.get(x, y, z);
                    if (state.isAir()) {
                        continue;
                    }

                    int worldY = y + minWorldY;
                    templatePos.set(x + minWorldX, worldY, z + minWorldZ);

                    section.setBlockState(x, y, z, state, false);

                    oceanFloor.trackUpdate(x, worldY, z, state);
                    worldSurface.trackUpdate(x, worldY, z, state);

                    if (state.getLuminance() != 0) {
                        chunk.addLightSource(templatePos);
                    }

                    var blockEntityTag = this.template.getBlockEntityTag(templatePos);
                    if (blockEntityTag != null) {
                        chunk.addPendingBlockEntityNbt(blockEntityTag);
                    }
                }
            }
        }
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        var chunkPos = region.getCenterPos();

        var chunkBounds = BlockBounds.ofChunk(chunkPos, region);
        if (!this.worldBounds.intersects(chunkBounds)) {
            return;
        }

        var protoChunk = (ProtoChunk) region.getChunk(chunkPos.x, chunkPos.z);

        int minSectionY = this.worldBounds.min().getY() >> 4;
        int maxSectionY = this.worldBounds.max().getY() >> 4;

        for (int sectionY = maxSectionY; sectionY >= minSectionY; sectionY--) {
            this.template.getEntitiesInChunk(chunkPos.x, sectionY, chunkPos.z).forEach(entity -> {
                var entityTag = entity.createEntityTag(BlockPos.ORIGIN);
                protoChunk.addEntity(entityTag);
            });
        }
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
        if (this.worldBounds.contains(x, z)) {
            return this.template.getTopY(x, z, heightmap);
        }
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
        if (this.worldBounds.contains(x, z)) {
            var mutablePos = new BlockPos.Mutable(x, 0, z);

            int minY = this.worldBounds.min().getY();
            int maxY = this.worldBounds.max().getY();

            var column = new BlockState[maxY - minY + 1];
            for (int y = maxY; y >= minY; y--) {
                mutablePos.setY(y);
                column[y] = this.template.getBlockState(mutablePos);
            }

            return new VerticalBlockSample(minY, column);
        }

        return GeneratorBlockSamples.VOID;
    }
}
