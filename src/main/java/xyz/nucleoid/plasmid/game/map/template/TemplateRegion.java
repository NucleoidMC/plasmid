package xyz.nucleoid.plasmid.game.map.template;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class TemplateRegion {
    private final String marker;
    private final BlockBounds bounds;
    private final CompoundTag data;

    public TemplateRegion(String marker, BlockBounds bounds, @Nullable CompoundTag data) {
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
     * Returns an optional extra data assigned to this region.
     *
     * @return The extra data.
     */
    public @Nullable CompoundTag getData() {
        return this.data;
    }

    public CompoundTag serialize(CompoundTag tag) {
        tag.putString("marker", this.marker);
        this.bounds.serialize(tag);
        if (this.data == null) { tag.put("data", new CompoundTag()); } else { tag.put("data", this.data); }
        return tag;
    }

    public static TemplateRegion deserialize(CompoundTag tag) {
        String marker = tag.getString("marker");
        CompoundTag data = tag.getCompound("data");
        return new TemplateRegion(marker, BlockBounds.deserialize(tag), data);
    }
}
