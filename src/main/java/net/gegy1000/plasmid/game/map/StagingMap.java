package net.gegy1000.plasmid.game.map;

import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class StagingMap {
    private final ServerWorld world;
    private final Identifier identifier;
    private final BlockBounds bounds;

    private final List<GameRegion> regions = new ArrayList<>();

    public StagingMap(ServerWorld world, Identifier identifier, BlockBounds bounds) {
        this.world = world;
        this.identifier = identifier;
        this.bounds = bounds;
    }

    private void setDirty() {
        StagingMapManager.get(this.world).setDirty(true);
    }

    public void addRegion(String marker, BlockBounds bounds) {
        this.regions.add(new GameRegion(marker, bounds));
        this.setDirty();
    }

    public void removeRegion(GameRegion region) {
        this.regions.remove(region);
        this.setDirty();
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }

    public Collection<GameRegion> getRegions() {
        return this.regions;
    }

    public CompoundTag serialize(CompoundTag root) {
        root.putString("identifier", this.identifier.toString());
        this.bounds.serialize(root);

        ListTag regionList = new ListTag();
        for (GameRegion region : this.regions) {
            regionList.add(region.serialize(new CompoundTag()));
        }
        root.put("regions", regionList);

        return root;
    }

    public static StagingMap deserialize(ServerWorld world, CompoundTag root) {
        Identifier identifier = new Identifier(root.getString("identifier"));

        BlockBounds bounds = BlockBounds.deserialize(root);

        StagingMap map = new StagingMap(world, identifier, bounds);
        ListTag regionList = root.getList("regions", 10);
        for (int i = 0; i < regionList.size(); i++) {
            CompoundTag regionRoot = regionList.getCompound(i);
            map.regions.add(GameRegion.deserialize(regionRoot));
        }

        return map;
    }

    public GameMapData compile() {
        GameMapData map = new GameMapData(this.identifier);
        map.bounds = this.globalToLocal(this.bounds);

        for (GameRegion region : this.regions) {
            map.regions.add(new GameRegion(
                    region.getMarker(),
                    this.globalToLocal(region.getBounds())
            ));
        }

        for (BlockPos pos : this.bounds.iterate()) {
            BlockPos localPos = this.globalToLocal(pos);

            BlockEntity entity = this.world.getBlockEntity(pos);
            if (entity != null) {
                CompoundTag entityTag = entity.toTag(new CompoundTag());
                entityTag.putInt("x", localPos.getX());
                entityTag.putInt("y", localPos.getY());
                entityTag.putInt("z", localPos.getZ());
                map.blockEntities.put(localPos.asLong(), entityTag);
            }

            BlockState state = this.world.getBlockState(pos);
            map.setBlockState(localPos, state);
        }

        return map;
    }

    private BlockPos globalToLocal(BlockPos pos) {
        return pos.subtract(this.bounds.getMin());
    }

    private BlockBounds globalToLocal(BlockBounds bounds) {
        return new BlockBounds(this.globalToLocal(bounds.getMin()), this.globalToLocal(bounds.getMax()));
    }
}
