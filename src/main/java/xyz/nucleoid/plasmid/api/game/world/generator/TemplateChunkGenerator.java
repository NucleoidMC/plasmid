package xyz.nucleoid.plasmid.api.game.world.generator;

import net.minecraft.block.BlockState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapChunk;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.concurrent.CompletableFuture;

public class TemplateChunkGenerator extends GameChunkGenerator {
    private final MapTemplate template;
    private final BlockBounds worldBounds;

    public TemplateChunkGenerator(MinecraftServer server, MapTemplate template) {
        super(createBiomeSource(server, template.getBiome()));

        this.template = template;
        this.worldBounds = template.getBounds();
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registryManager, StructurePlacementCalculator placementCalculator, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager, RegistryKey<World> dimension) {
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
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
        });
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

                    var blockEntityTag = this.template.getBlockEntityNbt(templatePos);
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
                var entityTag = entity.createEntityNbt(BlockPos.ORIGIN);
                protoChunk.addEntity(entityTag);
            });
        }
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        if (this.worldBounds.contains(x, z)) {
            return this.template.getTopY(x, z, heightmap);
        }
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
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
