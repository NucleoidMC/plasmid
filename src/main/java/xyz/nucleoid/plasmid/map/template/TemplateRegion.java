package xyz.nucleoid.plasmid.map.template;

import net.minecraft.nbt.CompoundTag;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class TemplateRegion {
    private final String marker;
    private final BlockBounds bounds;
    private CompoundTag data;

    public TemplateRegion(String marker, BlockBounds bounds, CompoundTag data) {
        this.marker = marker;
        this.bounds = bounds;
        this.data = data;
    }

    public String getMarker() {
        return this.marker;
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }

    /**
     * Returns the extra data assigned to this region.
     *
     * @return the extra data
     */
    public CompoundTag getData() {
        return this.data;
    }

    /**
     * Sets the extra data assigned to this region.
     *
     * @param data the extra data
     */
    public void setData(CompoundTag data) {
        this.data = data;
    }

    public CompoundTag serialize(CompoundTag tag) {
        tag.putString("marker", this.marker);
        this.bounds.serialize(tag);
        tag.put("data", this.data);
        return tag;
    }

    public static TemplateRegion deserialize(CompoundTag tag) {
        String marker = tag.getString("marker");
        CompoundTag data = tag.getCompound("data");
        return new TemplateRegion(marker, BlockBounds.deserialize(tag), data);
    }
}
