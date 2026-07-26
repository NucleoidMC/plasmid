package xyz.nucleoid.plasmid.api.game.level.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
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
    public void createStructures(RegistryAccess registryManager, ChunkGeneratorStructureState placementCalculator, StructureManager structureAccessor, ChunkAccess chunk, StructureTemplateManager structureTemplateManager, ResourceKey<Level> dimension) {
    }

    @Override
    public void createReferences(WorldGenLevel world, StructureManager accessor, ChunkAccess chunk) {
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState noiseConfig, StructureManager structureAccessor, ChunkAccess chunk) {
        var chunkPos = chunk.getPos();

        var chunkBounds = BlockBounds.ofChunk(chunk);
        if (!this.worldBounds.intersects(chunkBounds)) {
            return CompletableFuture.completedFuture(chunk);
        }

        return CompletableFuture.supplyAsync(() -> {
            var protoChunk = (ProtoChunk) chunk;
            var mutablePos = new BlockPos.MutableBlockPos();

            int minLevelX = chunkPos.getMinBlockX();
            int minLevelZ = chunkPos.getMinBlockZ();

            int minSectionY = this.worldBounds.min().getY() >> 4;
            int maxSectionY = this.worldBounds.max().getY() >> 4;

            for (int sectionY = maxSectionY; sectionY >= minSectionY; sectionY--) {
                long sectionPos = SectionPos.asLong(chunkPos.x(), sectionY, chunkPos.z());

                var templateChunk = this.template.getChunk(sectionPos);
                if (templateChunk == null) {
                    continue;
                }

                var section = protoChunk.getSection(protoChunk.getSectionIndexFromSectionY(sectionY));
                section.acquire();

                try {
                    int minLevelY = sectionY << 4;
                    this.addSection(minLevelX, minLevelY, minLevelZ, mutablePos, protoChunk, section, templateChunk);
                } finally {
                    section.release();
                }
            }

            return chunk;
        });
    }

    private void addSection(int minLevelX, int minLevelY, int minLevelZ, BlockPos.MutableBlockPos templatePos, ProtoChunk chunk, LevelChunkSection section, MapChunk templateChunk) {
        var oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        var worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    var state = templateChunk.get(x, y, z);
                    if (state.isAir()) {
                        continue;
                    }

                    int worldY = y + minLevelY;
                    templatePos.set(x + minLevelX, worldY, z + minLevelZ);

                    section.setBlockState(x, y, z, state, false);

                    oceanFloor.update(x, worldY, z, state);
                    worldSurface.update(x, worldY, z, state);

                    if (state.getBlock() instanceof EntityBlock entityBlock) {
                        var blockEntityTag = this.template.getBlockEntityNbt(templatePos);
                        if (blockEntityTag != null) {
                            chunk.setBlockEntityNbt(blockEntityTag);
                        } else {
                            var be = entityBlock.newBlockEntity(new BlockPos(templatePos), state);
                            if (be != null) {
                                chunk.setBlockEntity(be);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        var chunkPos = region.getCenter();

        var chunkBounds = BlockBounds.ofChunk(chunkPos, region);
        if (!this.worldBounds.intersects(chunkBounds)) {
            return;
        }

        var protoChunk = (ProtoChunk) region.getChunk(chunkPos.x(), chunkPos.z());

        int minSectionY = this.worldBounds.min().getY() >> 4;
        int maxSectionY = this.worldBounds.max().getY() >> 4;

        for (int sectionY = maxSectionY; sectionY >= minSectionY; sectionY--) {
            this.template.getEntitiesInChunk(chunkPos.x(), sectionY, chunkPos.z()).forEach(entity -> {
                var entityTag = entity.createEntityNbt(BlockPos.ZERO);
                protoChunk.addEntity(entityTag);
            });
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world, RandomState noiseConfig) {
        if (this.worldBounds.contains(x, z)) {
            return this.template.getTopY(x, z, heightmap);
        }
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState noiseConfig) {
        if (this.worldBounds.contains(x, z)) {
            var mutablePos = new BlockPos.MutableBlockPos(x, 0, z);

            int minY = this.worldBounds.min().getY();
            int maxY = this.worldBounds.max().getY();

            var column = new BlockState[maxY - minY + 1];
            for (int y = maxY; y >= minY; y--) {
                mutablePos.setY(y);
                column[y] = this.template.getBlockState(mutablePos);
            }

            return new NoiseColumn(minY, column);
        }

        return GeneratorBlockSamples.VOID;
    }
}
