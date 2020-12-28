package xyz.nucleoid.plasmid.map.template;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class MapEntity {
    private final Vec3d position;
    final CompoundTag tag;

    MapEntity(Vec3d position, CompoundTag tag) {
        this.position = position;
        this.tag = tag;
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public CompoundTag createEntityTag(BlockPos origin) {
        CompoundTag tag = this.tag.copy();

        Vec3d chunkLocalPos = listToPos(this.tag.getList("Pos", NbtType.DOUBLE));

        Vec3d worldPosition = this.position.add(origin.getX(), origin.getY(), origin.getZ());
        tag.put("Pos", posToList(worldPosition));

        if (tag.contains("TileX", NbtType.INT)) {
            tag.putInt("TileX", MathHelper.floor(tag.getInt("TileX") + worldPosition.x - chunkLocalPos.x));
            tag.putInt("TileY", MathHelper.floor(tag.getInt("TileY") + worldPosition.y - chunkLocalPos.y));
            tag.putInt("TileZ", MathHelper.floor(tag.getInt("TileZ") + worldPosition.z - chunkLocalPos.z));
        }

        return tag;
    }

    public void createEntities(World world, BlockPos origin, Consumer<Entity> consumer) {
        CompoundTag tag = this.createEntityTag(origin);
        EntityType.loadEntityWithPassengers(tag, world, entity -> {
            consumer.accept(entity);
            return entity;
        });
    }

    @Nullable
    public static MapEntity fromEntity(Entity entity, Vec3d position) {
        CompoundTag tag = new CompoundTag();
        if (!entity.saveToTag(tag)) {
            return null;
        }

        // Avoid conflicts.
        tag.remove("UUID");

        int minChunkX = MathHelper.floor(position.getX()) & ~15;
        int minChunkY = MathHelper.floor(position.getY()) & ~15;
        int minChunkZ = MathHelper.floor(position.getZ()) & ~15;

        tag.put("Pos", posToList(position.subtract(minChunkX, minChunkY, minChunkZ)));

        // AbstractDecorationEntity has special position handling with an attachment position.
        if (entity instanceof AbstractDecorationEntity) {
            BlockPos blockPos = ((AbstractDecorationEntity) entity).getDecorationBlockPos()
                    .subtract(entity.getBlockPos())
                    .add(position.getX(), position.getY(), position.getZ());
            tag.putInt("TileX", blockPos.getX() - minChunkX);
            tag.putInt("TileY", blockPos.getY() - minChunkY);
            tag.putInt("TileZ", blockPos.getZ() - minChunkZ);
        }

        return new MapEntity(position, tag);
    }

    public static MapEntity fromTag(ChunkSectionPos sectionPos, CompoundTag tag) {
        Vec3d localPos = listToPos(tag.getList("Pos", NbtType.DOUBLE));
        Vec3d globalPos = localPos.add(sectionPos.getMinX(), sectionPos.getMinY(), sectionPos.getMinZ());

        return new MapEntity(globalPos, tag);
    }

    private static ListTag posToList(Vec3d pos) {
        ListTag list = new ListTag();
        list.add(DoubleTag.of(pos.x));
        list.add(DoubleTag.of(pos.y));
        list.add(DoubleTag.of(pos.z));
        return list;
    }

    private static Vec3d listToPos(ListTag list) {
        return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }
}
