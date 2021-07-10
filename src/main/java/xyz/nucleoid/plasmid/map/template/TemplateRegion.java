package xyz.nucleoid.plasmid.map.template;

import net.minecraft.nbt.NbtCompound;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class TemplateRegion {
    private final String marker;
    private final BlockBounds bounds;
    private NbtCompound data;

    public TemplateRegion(String marker, BlockBounds bounds, NbtCompound data) {
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
    public NbtCompound getData() {
        return this.data;
    }

    /**
     * Sets the extra data assigned to this region.
     *
     * @param data the extra data
     */
    public void setData(NbtCompound data) {
        this.data = data;
    }

    public NbtCompound serialize(NbtCompound tag) {
        tag.putString("marker", this.marker);
        this.bounds.serialize(tag);
        tag.put("data", this.data);
        return tag;
    }

    public static TemplateRegion deserialize(NbtCompound tag) {
        String marker = tag.getString("marker");
        NbtCompound data = tag.getCompound("data");
        return new TemplateRegion(marker, BlockBounds.deserialize(tag), data);
    }

    public TemplateRegion copy() {
        return new TemplateRegion(this.marker, this.bounds, this.data != null ? this.data.copy() : null);
    }
}
