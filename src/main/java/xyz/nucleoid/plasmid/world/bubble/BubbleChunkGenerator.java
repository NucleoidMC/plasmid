package xyz.nucleoid.plasmid.world.bubble;

import com.mojang.serialization.Codec;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.*;
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
import net.minecraft.world.gen.feature.StructureFeature;
import xyz.nucleoid.plasmid.game.world.generator.VoidChunkGenerator;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class BubbleChunkGenerator extends ChunkGenerator {
    public static final Codec<BubbleChunkGenerator> CODEC = RegistryLookupCodec.of(Registry.BIOME_KEY)
            .xmap(BubbleChunkGenerator::new, g -> g.biomeRegistry)
            .stable().codec();

    private static final StructuresConfig VOID_STRUCTURES = new StructuresConfig(Optional.empty(), Collections.emptyMap());

    private final Registry<Biome> biomeRegistry;

    private ChunkGenerator generator;

    public BubbleChunkGenerator(Registry<Biome> biomeRegistry) {
        super(new FixedBiomeSource(biomeRegistry.get(BuiltinBiomes.THE_VOID)), VOID_STRUCTURES);
        this.biomeRegistry = biomeRegistry;

        this.clearGenerator();
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    public void setGenerator(ChunkGenerator generator) {
        this.generator = generator;
    }

    public void clearGenerator() {
        this.generator = new VoidChunkGenerator(this.biomeRegistry);
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registry, StructureAccessor accessor, Chunk chunk, StructureManager manager, long seed) {
        this.generator.setStructureStarts(registry, accessor, chunk, manager, seed);
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor structures, Chunk chunk) {
        this.generator.addStructureReferences(world, structures, chunk);
    }

    @Override
    public void populateBiomes(Registry<Biome> registry, Chunk chunk) {
        this.generator.populateBiomes(registry, chunk);
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
    public StructuresConfig getStructuresConfig() {
        return this.generator.getStructuresConfig();
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
    public boolean isStrongholdStartingChunk(ChunkPos pos) {
        return this.generator.isStrongholdStartingChunk(pos);
    }

    @Override
    public List<SpawnSettings.SpawnEntry> getEntitySpawnList(Biome biome, StructureAccessor structures, SpawnGroup group, BlockPos pos) {
        return this.generator.getEntitySpawnList(biome, structures, group, pos);
    }
}
