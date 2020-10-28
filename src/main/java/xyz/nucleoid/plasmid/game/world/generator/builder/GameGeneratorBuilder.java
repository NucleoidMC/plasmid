package xyz.nucleoid.plasmid.game.world.generator.builder;

import com.mojang.serialization.Codec;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import xyz.nucleoid.plasmid.game.world.generator.VoidChunkGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class GameGeneratorBuilder {
    StructuresConfig structures = new StructuresConfig(Optional.empty(), Collections.emptyMap());
    BiomeSource biomes = new FixedBiomeSource(BuiltinBiomes.THE_VOID);
    GameSurfaceComposer surface = GameSurfaceComposer.none();
    GameFeatureComposer features = GameFeatureComposer.none();

    int seaLevel = 63;

    public GameGeneratorBuilder setStructures(StructuresConfig structures) {
        this.structures = structures;
        return this;
    }

    public GameGeneratorBuilder setBiomes(BiomeSource biomes) {
        this.biomes = biomes;
        return this;
    }

    public GameGeneratorBuilder setSurface(GameSurfaceComposer surface) {
        this.surface = surface;
        return this;
    }

    public GameGeneratorBuilder setFeatures(GameFeatureComposer features) {
        this.features = features;
        return this;
    }

    public GameGeneratorBuilder setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
        return this;
    }

    public ChunkGenerator build() {
        return new Generator(this);
    }

    private static final class Generator extends ChunkGenerator {
        private final GameSurfaceComposer surface;
        private final GameFeatureComposer features;
        private final int seaLevel;

        Generator(GameGeneratorBuilder builder) {
            super(builder.biomes, builder.structures);
            this.surface = builder.surface;
            this.features = builder.features;
            this.seaLevel = builder.seaLevel;
        }

        @Override
        protected Codec<? extends ChunkGenerator> getCodec() {
            return VoidChunkGenerator.CODEC;
        }

        @Override
        public ChunkGenerator withSeed(long seed) {
            return this;
        }

        @Override
        public void populateNoise(WorldAccess worldAccess, StructureAccessor structures, Chunk chunk) {
            BiomeAccess biomes = worldAccess.getBiomeAccess();
            ChunkRegion region = (ChunkRegion) worldAccess;

            this.surface.addSurface(chunk, region, biomes, structures);
        }

        @Override
        public void buildSurface(ChunkRegion region, Chunk chunk) {
        }

        @Override
        public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
            this.features.addFeatures(region, structures);
        }

        @Override
        public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
        }

        @Override
        public void populateEntities(ChunkRegion region) {
        }

        @Override
        public int getHeight(int x, int z, Heightmap.Type heightmapType) {
            return this.surface.sampleHeight(x, z, heightmapType);
        }

        @Override
        public BlockView getColumnSample(int x, int z) {
            return this.surface.sampleColumn(x, z);
        }

        @Override
        public List<SpawnSettings.SpawnEntry> getEntitySpawnList(Biome biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
            return Collections.emptyList();
        }

        @Override
        public int getSpawnHeight() {
            return this.seaLevel + 1;
        }

        @Override
        public int getSeaLevel() {
            return this.seaLevel;
        }
    }
}
