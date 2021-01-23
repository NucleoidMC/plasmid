package xyz.nucleoid.plasmid.map.workspace;

import net.minecraft.nbt.CompoundTag;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class WorkspaceRegion {
    public final int runtimeId;
    public final String marker;
    public final BlockBounds bounds;
    public final CompoundTag data;

    public WorkspaceRegion(int runtimeId, String marker, BlockBounds bounds, CompoundTag data) {
        this.runtimeId = runtimeId;
        this.marker = marker;
        this.bounds = bounds;
        this.data = data;
    }

    public WorkspaceRegion withMarker(String marker) {
        return new WorkspaceRegion(this.runtimeId, marker, this.bounds, this.data);
    }

    public WorkspaceRegion withBounds(BlockBounds bounds) {
        return new WorkspaceRegion(this.runtimeId, this.marker, bounds, this.data);
    }

    public WorkspaceRegion withData(CompoundTag data) {
        return new WorkspaceRegion(this.runtimeId, this.marker, this.bounds, data);
    }

    public CompoundTag serialize(CompoundTag tag) {
        tag.putString("marker", this.marker);
        this.bounds.serialize(tag);
        tag.put("data", this.data);
        return tag;
    }

    public static WorkspaceRegion deserialize(int runtimeId, CompoundTag tag) {
        String marker = tag.getString("marker");
        CompoundTag data = tag.getCompound("data");
        return new WorkspaceRegion(runtimeId, marker, BlockBounds.deserialize(tag), data);
    }
}
