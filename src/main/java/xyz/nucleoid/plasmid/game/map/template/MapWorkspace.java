package xyz.nucleoid.plasmid.game.map.template;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import xyz.nucleoid.fantasy.PersistentWorldHandle;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.*;

/**
 * A map workspace represents an in-world map template within a dimension before it has been compiled to a static file.
 * <p>
 * It stores regions and arbitrary data destined to be compiled into a {@link MapTemplate}.
 */
public final class MapWorkspace {
    private final PersistentWorldHandle worldHandle;

    private final Identifier identifier;
    private final BlockBounds bounds;

    /* Regions */
    private final List<TemplateRegion> regions = new ArrayList<>();

    /* Entities */
    private final Set<UUID> entitiesToInclude = new ObjectOpenHashSet<>();
    private final Set<EntityType<?>> entityTypesToInclude = new ObjectOpenHashSet<>();

    /* Data */
    private CompoundTag data = new CompoundTag();

    public MapWorkspace(PersistentWorldHandle worldHandle, Identifier identifier, BlockBounds bounds) {
        this.worldHandle = worldHandle;
        this.identifier = identifier;
        this.bounds = bounds;
    }

    public void addRegion(String marker, BlockBounds bounds, CompoundTag tag) {
        this.regions.add(new TemplateRegion(marker, bounds, tag));
    }

    public void removeRegion(TemplateRegion region) {
        this.regions.remove(region);
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
        return this.entitiesToInclude.add(entity);
    }

    public boolean containsEntity(UUID entity) {
        return this.entitiesToInclude.contains(entity);
    }

    public boolean removeEntity(UUID entity) {
        return this.entitiesToInclude.remove(entity);
    }

    public boolean addEntityType(EntityType<?> type) {
        return this.entityTypesToInclude.add(type);
    }

    public boolean hasEntityType(EntityType<?> type) {
        return this.entityTypesToInclude.contains(type);
    }

    public boolean removeEntityType(EntityType<?> type) {
        return this.entityTypesToInclude.remove(type);
    }

    /**
     * Gets the arbitrary data of the map.
     *
     * @return the data as a compound tag
     */
    public CompoundTag getData() {
        return this.data;
    }

    /**
     * Sets the arbitrary data of the map.
     *
     * @param data the data as a compound tag
     */
    public void setData(CompoundTag data) {
        this.data = data;
    }

    public CompoundTag serialize(CompoundTag root) {
        root.putString("identifier", this.identifier.toString());
        this.bounds.serialize(root);

        // Regions
        ListTag regionList = new ListTag();
        for (TemplateRegion region : this.regions) {
            regionList.add(region.serialize(new CompoundTag()));
        }
        root.put("regions", regionList);

        // Entities
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

        // Data
        root.put("data", this.getData());

        return root;
    }

    public static MapWorkspace deserialize(PersistentWorldHandle worldHandle, CompoundTag root) {
        Identifier identifier = new Identifier(root.getString("identifier"));
        BlockBounds bounds = BlockBounds.deserialize(root);

        MapWorkspace map = new MapWorkspace(worldHandle, identifier, bounds);

        // Regions
        ListTag regionList = root.getList("regions", NbtType.COMPOUND);
        for (int i = 0; i < regionList.size(); i++) {
            CompoundTag regionRoot = regionList.getCompound(i);
            map.regions.add(TemplateRegion.deserialize(regionRoot));
        }

        // Entities
        CompoundTag entitiesTag = root.getCompound("entities");
        entitiesTag.getList("uuids", NbtType.INT_ARRAY).stream()
                .map(NbtHelper::toUuid)
                .forEach(map.entitiesToInclude::add);

        entitiesTag.getList("types", NbtType.STRING).stream()
                .map(tag -> new Identifier(tag.asString()))
                .map(Registry.ENTITY_TYPE::get)
                .forEach(map.entityTypesToInclude::add);

        // Data
        map.data = root.getCompound("data");

        return map;
    }

    /**
     * Compiles this map workspace into a map template.
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
        map.setData(this.getData());

        for (TemplateRegion region : this.regions) {
            map.addRegion(
                    region.getMarker(),
                    this.globalToLocal(region.getBounds()),
                    region.getData()
            );
        }

        ServerWorld world = this.worldHandle.asWorld();

        for (BlockPos pos : this.bounds.iterate()) {
            BlockPos localPos = this.globalToLocal(pos);

            BlockState state = world.getBlockState(pos);
            map.setBlockState(localPos, state);

            BlockEntity entity = world.getBlockEntity(pos);
            if (entity != null) {
                map.setBlockEntity(localPos, entity);
            }
        }

        if (includeEntities) {
            world.getEntitiesByClass(Entity.class, this.bounds.toBox(), entity -> !entity.removed
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

    public ServerWorld getWorld() {
        return this.worldHandle.asWorld();
    }
}
