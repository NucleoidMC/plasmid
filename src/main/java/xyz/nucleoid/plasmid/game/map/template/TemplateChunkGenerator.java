package xyz.nucleoid.plasmid.game.map.template;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;
import xyz.nucleoid.plasmid.game.world.view.VoidBlockView;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

public class TemplateChunkGenerator extends GameChunkGenerator {
    private final MapTemplate template;
    private final BlockBounds worldBounds;
    private final BlockPos origin;

    public TemplateChunkGenerator(MinecraftServer server, MapTemplate template, BlockPos origin) {
        super(createBiomeSource(server, template.getBiome()), new StructuresConfig(Optional.empty(), Collections.emptyMap()));

        this.template = template;
        this.worldBounds = template.getBounds().offset(origin);
        this.origin = origin;
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registryManager, StructureAccessor accessor, Chunk chunk, StructureManager manager, long seed) {
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
    }

    @Override
    public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        BlockBounds chunkBounds = BlockBounds.of(chunkPos);
        if (!this.worldBounds.intersects(chunkBounds)) {
            return;
        }

        ProtoChunk protoChunk = (ProtoChunk) chunk;
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        int minWorldX = chunkPos.getStartX();
        int minWorldZ = chunkPos.getStartZ();

        int minSectionY = this.worldBounds.getMin().getY() >> 4;
        int maxSectionY = this.worldBounds.getMax().getY() >> 4;

        for (int sectionY = maxSectionY; sectionY >= minSectionY; sectionY--) {
            ChunkSection section = protoChunk.getSection(sectionY);
            section.lock();

            try {
                int minWorldY = sectionY << 4;
                this.addSection(minWorldX, minWorldY, minWorldZ, mutablePos, protoChunk, section);
            } finally {
                section.unlock();
            }
        }
    }

    private void addSection(int minWorldX, int minWorldY, int minWorldZ, BlockPos.Mutable mutablePos, ProtoChunk chunk, ChunkSection section) {
        int offsetX = minWorldX - this.origin.getX();
        int offsetY = minWorldY - this.origin.getY();
        int offsetZ = minWorldZ - this.origin.getZ();

        Heightmap oceanFloor = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    mutablePos.set(x + offsetX, y + offsetY, z + offsetZ);

                    BlockState state = this.template.getBlockState(mutablePos);
                    if (!state.isAir()) {
                        section.setBlockState(x, y, z, state);

                        int worldY = y + minWorldY;
                        oceanFloor.trackUpdate(x, worldY, z, state);
                        worldSurface.trackUpdate(x, worldY, z, state);

                        if (state.getLuminance() != 0) {
                            chunk.addLightSource(new BlockPos(minWorldX + x, worldY, minWorldZ + z));
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
        if (this.worldBounds.contains(x, z)) {
            Predicate<BlockState> predicate = heightmapType.getBlockPredicate();
            BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, 0, z);

            int minY = this.worldBounds.getMin().getY();
            int maxY = this.worldBounds.getMax().getY();

            for (int y = maxY; y >= minY; y--) {
                mutablePos.setY(y);

                BlockState state = this.template.getBlockState(mutablePos);
                if (predicate.test(state)) {
                    return y;
                }
            }
        }

        return 0;
    }

    @Override
    public BlockView getColumnSample(int x, int z) {
        if (this.worldBounds.contains(x, z)) {
            BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, 0, z);

            int minY = this.worldBounds.getMin().getY();
            int maxY = this.worldBounds.getMax().getY();

            BlockState[] column = new BlockState[maxY + 1];
            Arrays.fill(column, minY, 0, Blocks.AIR.getDefaultState());

            for (int y = maxY; y >= minY; y--) {
                mutablePos.setY(y);
                column[y] = this.template.getBlockState(mutablePos);
            }

            return new VerticalBlockSample(column);
        }

        return VoidBlockView.INSTANCE;
    }
}
