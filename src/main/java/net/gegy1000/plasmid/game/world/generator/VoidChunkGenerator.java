package net.gegy1000.plasmid.game.world.generator;

import com.mojang.serialization.Codec;
import net.gegy1000.plasmid.game.world.view.VoidBlockView;
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
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;

public class VoidChunkGenerator extends ChunkGenerator {
    public static final VoidChunkGenerator INSTANCE = new VoidChunkGenerator();
    public static final Codec<VoidChunkGenerator> CODEC = Codec.unit(VoidChunkGenerator.INSTANCE);

    private VoidChunkGenerator() {
        this(Biomes.THE_VOID);
    }

    protected VoidChunkGenerator(Biome biome) {
        super(new FixedBiomeSource(biome), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
    }

    @Override
    protected Codec<? extends ChunkGenerator> method_28506() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void setStructureStarts(StructureAccessor structureAccessor, Chunk chunk, StructureManager structureManager, long l) {
    }

    @Override
    public void addStructureReferences(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
    }

    @Override
    public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor accessor) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
        return 0;
    }

    @Nullable
    @Override
    public BlockPos locateStructure(ServerWorld world, StructureFeature<?> feature, BlockPos center, int radius, boolean skipExistingChunks) {
        return null;
    }

    @Override
    public boolean method_28507(ChunkPos chunkPos) {
        return false;
    }

    @Override
    public BlockView getColumnSample(int x, int z) {
        return VoidBlockView.INSTANCE;
    }
}
