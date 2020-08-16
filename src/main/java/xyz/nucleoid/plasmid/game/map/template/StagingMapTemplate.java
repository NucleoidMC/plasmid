package xyz.nucleoid.plasmid.game.map.template;

import net.fabricmc.fabric.api.util.NbtType;
import xyz.nucleoid.plasmid.util.BlockBounds;
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

public final class StagingMapTemplate {
    private final ServerWorld world;
    private final Identifier identifier;
    private final BlockBounds bounds;

    private final List<TemplateRegion> regions = new ArrayList<>();

    public StagingMapTemplate(ServerWorld world, Identifier identifier, BlockBounds bounds) {
        this.world = world;
        this.identifier = identifier;
        this.bounds = bounds;
    }

    private void setDirty() {
        StagingMapManager.get(this.world).setDirty(true);
    }

    public void addRegion(String marker, BlockBounds bounds) {
        this.regions.add(new TemplateRegion(marker, bounds));
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

    public CompoundTag serialize(CompoundTag root) {
        root.putString("identifier", this.identifier.toString());
        this.bounds.serialize(root);

        ListTag regionList = new ListTag();
        for (TemplateRegion region : this.regions) {
            regionList.add(region.serialize(new CompoundTag()));
        }
        root.put("regions", regionList);

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

        return map;
    }

    public MapTemplate compile() {
        MapTemplate map = MapTemplate.createEmpty();
        map.bounds = this.globalToLocal(this.bounds);

        for (TemplateRegion region : this.regions) {
            map.addRegion(
                    region.getMarker(),
                    this.globalToLocal(region.getBounds())
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

        return map;
    }

    private BlockPos globalToLocal(BlockPos pos) {
        return pos.subtract(this.bounds.getMin());
    }

    private BlockBounds globalToLocal(BlockBounds bounds) {
        return new BlockBounds(this.globalToLocal(bounds.getMin()), this.globalToLocal(bounds.getMax()));
    }
}
