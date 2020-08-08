package net.gegy1000.plasmid.game.world.generator;

import com.mojang.serialization.Codec;
import net.gegy1000.plasmid.game.world.view.VoidBlockView;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;

import java.util.Collections;
import java.util.Optional;

public abstract class GameChunkGenerator extends ChunkGenerator {
    public GameChunkGenerator(BiomeSource biomes, StructuresConfig structures) {
        super(biomes, structures);
    }

    public GameChunkGenerator() {
        this(new FixedBiomeSource(Biomes.THE_VOID), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
    }

    @Override
    public final ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
    }

    @Override
    public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
        return 0;
    }

    @Override
    public BlockView getColumnSample(int x, int z) {
        return VoidBlockView.INSTANCE;
    }

    @Override
    protected final Codec<? extends ChunkGenerator> method_28506() {
        return VoidChunkGenerator.CODEC;
    }
}
