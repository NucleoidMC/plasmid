package xyz.nucleoid.plasmid.game.world.generator;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import xyz.nucleoid.plasmid.game.world.generator.view.VoidBlockView;

import java.util.Collections;
import java.util.Optional;

public abstract class GameChunkGenerator extends ChunkGenerator {
    public static final Codec<? extends ChunkGenerator> CODEC = new Codec<ChunkGenerator>() {
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
        DynamicRegistryManager registryManager = server.getRegistryManager();
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
    public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
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
    protected final Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }
}
