package xyz.nucleoid.plasmid.game.world.generator;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class GameChunkGenerator extends ChunkGenerator {
    public static final Codec<? extends ChunkGenerator> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ChunkGenerator, T>> decode(DynamicOps<T> ops, T input) {
            return Biome.REGISTRY_CODEC.decode(ops, ops.createString(BiomeKeys.THE_VOID.getValue().toString()))
                    .map(pair -> pair.mapFirst(VoidChunkGenerator::new));
        }

        @Override
        public <T> DataResult<T> encode(ChunkGenerator input, DynamicOps<T> ops, T prefix) {
            return DataResult.success(prefix);
        }
    };

    public GameChunkGenerator(BiomeSource biomes, StructuresConfig structures) {
        super(biomes, structures);
    }

    public GameChunkGenerator(MinecraftServer server) {
        this(createBiomeSource(server, BiomeKeys.THE_VOID), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
    }

    protected static FixedBiomeSource createBiomeSource(MinecraftServer server, RegistryKey<Biome> biome) {
        var registryManager = server.getRegistryManager();
        return new FixedBiomeSource(registryManager.get(Registry.BIOME_KEY).get(biome));
    }

    @Override
    public final ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
        return GeneratorBlockSamples.VOID;
    }

    @Override
    protected final Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }
}
