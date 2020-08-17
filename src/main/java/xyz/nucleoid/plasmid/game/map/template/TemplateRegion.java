package xyz.nucleoid.plasmid.game.map.template;

import net.minecraft.nbt.CompoundTag;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class TemplateRegion {
    private final String marker;
    private final BlockBounds bounds;

    public TemplateRegion(String marker, BlockBounds bounds) {
        this.marker = marker;
        this.bounds = bounds;
    }

    public String getMarker() {
        return this.marker;
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }

    public CompoundTag serialize(CompoundTag tag) {
        tag.putString("marker", this.marker);
        this.bounds.serialize(tag);
        return tag;
    }

    public static TemplateRegion deserialize(CompoundTag tag) {
        String marker = tag.getString("marker");
        return new TemplateRegion(marker, BlockBounds.deserialize(tag));
    }
}
