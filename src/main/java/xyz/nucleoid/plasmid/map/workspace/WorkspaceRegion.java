package xyz.nucleoid.plasmid.map.workspace;

import net.minecraft.nbt.NbtCompound;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class WorkspaceRegion {
    public final int runtimeId;
    public final String marker;
    public final BlockBounds bounds;
    public final NbtCompound data;

    public WorkspaceRegion(int runtimeId, String marker, BlockBounds bounds, NbtCompound data) {
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

    public WorkspaceRegion withData(NbtCompound data) {
        return new WorkspaceRegion(this.runtimeId, this.marker, this.bounds, data);
    }

    public NbtCompound serialize(NbtCompound tag) {
        tag.putString("marker", this.marker);
        this.bounds.serialize(tag);
        tag.put("data", this.data);
        return tag;
    }

    public static WorkspaceRegion deserialize(int runtimeId, NbtCompound tag) {
        var marker = tag.getString("marker");
        var data = tag.getCompound("data");
        return new WorkspaceRegion(runtimeId, marker, BlockBounds.deserialize(tag), data);
    }
}
