package xyz.nucleoid.plasmid.map.template;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import java.util.ArrayList;
import java.util.List;

final class MapChunk {
    private static final Palette<BlockState> PALETTE = new IdListPalette<>(Block.STATE_IDS, Blocks.AIR.getDefaultState());

    private final ChunkSectionPos pos;

    private final PalettedContainer<BlockState> container = new PalettedContainer<>(
            PALETTE, Block.STATE_IDS,
            NbtHelper::toBlockState, NbtHelper::fromBlockState,
            Blocks.AIR.getDefaultState()
    );
    private final List<MapEntity> entities = new ArrayList<>();

    MapChunk(ChunkSectionPos pos) {
        this.pos = pos;
    }

    public void set(int x, int y, int z, BlockState state) {
        this.container.set(x, y, z, state);
    }

    public BlockState get(int x, int y, int z) {
        return this.container.get(x, y, z);
    }

    /**
     * Adds an entity to this chunk.
     * <p>
     * The position of the entity must be relative to the map template.
     *
     * @param entity The entity to add.
     * @param position The entity position relative to the map.
     */
    public void addEntity(Entity entity, Vec3d position) {
        var mapEntity = MapEntity.fromEntity(entity, position);
        if (mapEntity != null) {
            this.entities.add(mapEntity);
        }
    }

    void addEntity(MapEntity entity) {
        this.entities.add(entity);
    }

    public ChunkSectionPos getPos() {
        return this.pos;
    }

    /**
     * Returns the entities in this chunk.
     *
     * @return The entities in this chunk.
     */
    public List<MapEntity> getEntities() {
        return this.entities;
    }

    public void serialize(NbtCompound tag) {
        this.container.write(tag, "palette", "block_states");

        if (!this.entities.isEmpty()) {
            var entitiesTag = new NbtList();
            for (var entity : this.entities) {
                entitiesTag.add(entity.tag);
            }
            tag.put("entities", entitiesTag);
        }
    }

    public static MapChunk deserialize(ChunkSectionPos pos, NbtCompound tag) {
        var chunk = new MapChunk(pos);
        chunk.container.read(tag.getList("palette", NbtType.COMPOUND), tag.getLongArray("block_states"));

        if (tag.contains("entities", NbtType.LIST)) {
            var entitiesTag = tag.getList("entities", NbtType.COMPOUND);
            for (var entityTag : entitiesTag) {
                chunk.entities.add(MapEntity.fromTag(pos, (NbtCompound) entityTag));
            }
        }

        return chunk;
    }
}
