package xyz.nucleoid.plasmid.game.world.generator;

import com.mojang.serialization.Codec;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class DynamicChunkGenerator extends ChunkGenerator {
    public static final Codec<DynamicChunkGenerator> CODEC = Codec.unit(DynamicChunkGenerator::new);

    private static final BiomeSource VOID_BIOMES = new FixedBiomeSource(Biomes.THE_VOID);
    private static final StructuresConfig VOID_STRUCTURES = new StructuresConfig(Optional.empty(), Collections.emptyMap());

    private ChunkGenerator generator = VoidChunkGenerator.INSTANCE;

    public DynamicChunkGenerator() {
        super(VOID_BIOMES, VOID_STRUCTURES);
    }

    @Override
    protected Codec<? extends ChunkGenerator> method_28506() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    public void setGenerator(ChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void setStructureStarts(StructureAccessor accessor, Chunk chunk, StructureManager manager, long seed) {
        this.generator.setStructureStarts(accessor, chunk, manager, seed);
    }

    @Override
    public void addStructureReferences(WorldAccess world, StructureAccessor structures, Chunk chunk) {
        this.generator.addStructureReferences(world, structures, chunk);
    }

    @Override
    public void populateBiomes(Chunk chunk) {
        this.generator.populateBiomes(chunk);
    }

    @Override
    public void carve(long seed, BiomeAccess biomes, Chunk chunk, GenerationStep.Carver step) {
        this.generator.carve(seed, biomes, chunk, step);
    }

    @Override
    public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
        this.generator.populateNoise(world, structures, chunk);
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
        this.generator.buildSurface(region, chunk);
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
        this.generator.generateFeatures(region, structures);
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        this.generator.populateEntities(region);
    }

    @Override
    public StructuresConfig getConfig() {
        return this.generator.getConfig();
    }

    @Override
    public int getSpawnHeight() {
        return this.generator.getSpawnHeight();
    }

    @Override
    public BiomeSource getBiomeSource() {
        return this.generator.getBiomeSource();
    }

    @Override
    public int getMaxY() {
        return this.generator.getMaxY();
    }

    @Override
    public int getSeaLevel() {
        return this.generator.getSeaLevel();
    }

    @Override
    public int getHeightOnGround(int x, int z, Heightmap.Type heightmapType) {
        return this.generator.getHeightOnGround(x, z, heightmapType);
    }

    @Override
    public int getHeightInGround(int x, int z, Heightmap.Type heightmapType) {
        return this.generator.getHeightInGround(x, z, heightmapType);
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
        return this.generator.getHeight(x, z, heightmapType);
    }

    @Override
    public BlockView getColumnSample(int x, int z) {
        return this.generator.getColumnSample(x, z);
    }

    @Nullable
    @Override
    public BlockPos locateStructure(ServerWorld world, StructureFeature<?> feature, BlockPos center, int radius, boolean skipExistingChunks) {
        return this.generator.locateStructure(world, feature, center, radius, skipExistingChunks);
    }

    @Override
    public boolean method_28507(ChunkPos pos) {
        return this.generator.method_28507(pos);
    }

    @Override
    public List<Biome.SpawnEntry> getEntitySpawnList(Biome biome, StructureAccessor structures, SpawnGroup group, BlockPos pos) {
        return this.generator.getEntitySpawnList(biome, structures, group, pos);
    }
}
