package net.gegy1000.plasmid.game.world.generator.builder;

import net.gegy1000.plasmid.game.world.view.HeightmapColumnSample;
import net.gegy1000.plasmid.game.world.view.VoidBlockView;
import net.minecraft.block.Blocks;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;

public interface GameSurfaceComposer {
    static GameSurfaceComposer none() {
        return new GameSurfaceComposer() {
            @Override
            public void addSurface(Chunk chunk, ChunkRegion region, BiomeAccess biomes, StructureAccessor structures) {
            }

            @Override
            public int sampleHeight(int x, int z, Heightmap.Type heightmapType) {
                return 0;
            }

            @Override
            public BlockView sampleColumn(int x, int z) {
                return VoidBlockView.INSTANCE;
            }
        };
    }

    void addSurface(Chunk chunk, ChunkRegion region, BiomeAccess biomes, StructureAccessor structures);

    int sampleHeight(int x, int z, Heightmap.Type heightmapType);

    default BlockView sampleColumn(int x, int z) {
        int height = this.sampleHeight(x, z, Heightmap.Type.WORLD_SURFACE);
        return new HeightmapColumnSample(height, Blocks.STONE.getDefaultState());
    }
}
