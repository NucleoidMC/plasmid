package xyz.nucleoid.plasmid.game.map.template;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.*;

/**
 * A staging map represents an in-world map template before it has been compiled to a static file.
 * <p>
 * It stores regions and arbitrary data destined to be compiled into a {@link MapTemplate}.
 */
public final class StagingMapTemplate {
    private final ServerWorld world;
    private final Identifier identifier;
    private final BlockBounds bounds;

    private final List<TemplateRegion> regions = new ArrayList<>();

    /* Entities */
    private final Set<UUID> entitiesToInclude = new HashSet<>();
    private final Set<EntityType<?>> entityTypesToInclude = new HashSet<>();

    public StagingMapTemplate(ServerWorld world, Identifier identifier, BlockBounds bounds) {
        this.world = world;
        this.identifier = identifier;
        this.bounds = bounds;
    }

    private void setDirty() {
        StagingMapManager.get(this.world).setDirty(true);
    }

    public void addRegion(String marker, BlockBounds bounds, CompoundTag tag) {
        this.regions.add(new TemplateRegion(marker, bounds, tag));
        this.setDirty();
    }

    public void removeRegion(TemplateRegion region) {
        this.regions.remove(region);
        this.setDirty();
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }

    public Collection<TemplateRegion> getRegions() {
        return this.regions;
    }

    public boolean addEntity(UUID entity) {
        boolean result = this.entitiesToInclude.add(entity);
        if (result) { this.setDirty(); }
        return result;
    }

    public boolean containsEntity(UUID entity) {
        return this.entitiesToInclude.contains(entity);
    }

    public boolean removeEntity(UUID entity) {
        boolean result = this.entitiesToInclude.remove(entity);
        if (result) { this.setDirty(); }
        return result;
    }

    public boolean addEntityType(EntityType<?> type) {
        boolean result = this.entityTypesToInclude.add(type);
        if (result) { this.setDirty(); }
        return result;
    }

    public boolean hasEntityType(EntityType<?> type) {
        return this.entityTypesToInclude.contains(type);
    }

    public boolean removeEntityType(EntityType<?> type) {
        boolean result = this.entityTypesToInclude.remove(type);
        if (result) { this.setDirty(); }
        return result;
    }

    public CompoundTag serialize(CompoundTag root) {
        root.putString("identifier", this.identifier.toString());
        this.bounds.serialize(root);

        ListTag regionList = new ListTag();
        for (TemplateRegion region : this.regions) {
            regionList.add(region.serialize(new CompoundTag()));
        }
        root.put("regions", regionList);

        CompoundTag entitiesTag = new CompoundTag();
        ListTag entityList = new ListTag();
        for (UUID uuid : this.entitiesToInclude) {
            entityList.add(NbtHelper.fromUuid(uuid));
        }
        entitiesTag.put("uuids", entityList);
        ListTag entityTypeList = new ListTag();
        for (EntityType<?> type : this.entityTypesToInclude) {
            entityTypeList.add(StringTag.of(Registry.ENTITY_TYPE.getId(type).toString()));
        }
        entitiesTag.put("types", entityTypeList);
        root.put("entities", entitiesTag);

        return root;
    }

    public static StagingMapTemplate deserialize(ServerWorld world, CompoundTag root) {
        Identifier identifier = new Identifier(root.getString("identifier"));

        BlockBounds bounds = BlockBounds.deserialize(root);

        StagingMapTemplate map = new StagingMapTemplate(world, identifier, bounds);
        ListTag regionList = root.getList("regions", NbtType.COMPOUND);
        for (int i = 0; i < regionList.size(); i++) {
            CompoundTag regionRoot = regionList.getCompound(i);
            map.regions.add(TemplateRegion.deserialize(regionRoot));
        }

        CompoundTag entitiesTag = root.getCompound("entities");
        ListTag entityList = entitiesTag.getList("uuids", NbtType.INT_ARRAY);
        entityList.stream().map(NbtHelper::toUuid).forEach(map.entitiesToInclude::add);
        ListTag entityTypeList = entitiesTag.getList("types", NbtType.STRING);
        entityTypeList.stream().map(tag -> new Identifier(tag.asString()))
                .map(Registry.ENTITY_TYPE::get)
                .forEach(map.entityTypesToInclude::add);

        return map;
    }

    /**
     * Compiles this staging map template into a map template.
     * <p>
     * It copies the block and entity data from the world and stores it within the template.
     * All positions are made relative.
     *
     * @param includeEntities True if entities should be included, else false.
     * @return The compiled map.
     */
    public MapTemplate compile(boolean includeEntities) {
        MapTemplate map = MapTemplate.createEmpty();
        map.bounds = this.globalToLocal(this.bounds);

        for (TemplateRegion region : this.regions) {
            map.addRegion(
                    region.getMarker(),
                    this.globalToLocal(region.getBounds()),
                    region.getData()
            );
        }

        for (BlockPos pos : this.bounds.iterate()) {
            BlockPos localPos = this.globalToLocal(pos);

            BlockState state = this.world.getBlockState(pos);
            map.setBlockState(localPos, state);

            BlockEntity entity = this.world.getBlockEntity(pos);
            if (entity != null) {
                map.setBlockEntity(localPos, entity);
            }
        }

        if (includeEntities) {
            this.world.getEntitiesByClass(Entity.class, this.bounds.toBox(), entity -> !entity.removed
                    && (this.containsEntity(entity.getUuid()) || this.hasEntityType(entity.getType())))
                    .forEach(entity -> map.addEntity(entity, this.globalToLocal(entity.getPos())));
        }

        return map;
    }

    private BlockPos globalToLocal(BlockPos pos) {
        return pos.subtract(this.bounds.getMin());
    }

    private Vec3d globalToLocal(Vec3d pos) {
        BlockPos min = this.bounds.getMin();
        return pos.subtract(min.getX(), min.getY(), min.getZ());
    }

    private BlockBounds globalToLocal(BlockBounds bounds) {
        return new BlockBounds(this.globalToLocal(bounds.getMin()), this.globalToLocal(bounds.getMax()));
    }
}
