package xyz.nucleoid.plasmid.game.world.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.world.generator.view.VoidBlockView;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public class VoidChunkGenerator extends ChunkGenerator {
    public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Biome.REGISTRY_CODEC.stable().fieldOf("biome").forGetter(g -> g.biome)
        ).apply(instance, instance.stable(VoidChunkGenerator::new));
    });

    private final Supplier<Biome> biome;

    public VoidChunkGenerator(Supplier<Biome> biome) {
        super(new FixedBiomeSource(biome), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
        this.biome = biome;
    }

    public VoidChunkGenerator(Registry<Biome> biomeRegistry) {
        this(biomeRegistry, BiomeKeys.THE_VOID);
    }

    public VoidChunkGenerator(Registry<Biome> biomeRegistry, RegistryKey<Biome> biome) {
        this(() -> biomeRegistry.get(biome));
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registryManager, StructureAccessor accessor, Chunk chunk, StructureManager manager, long seed) {
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
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
    public boolean isStrongholdStartingChunk(ChunkPos chunkPos) {
        return false;
    }

    @Override
    public BlockView getColumnSample(int x, int z) {
        return VoidBlockView.INSTANCE;
    }
}
