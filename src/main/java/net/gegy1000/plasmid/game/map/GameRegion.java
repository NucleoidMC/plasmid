package net.gegy1000.plasmid.game.map;

import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.nbt.CompoundTag;

public final class GameRegion {
    private final String marker;
    private final BlockBounds bounds;

    public GameRegion(String marker, BlockBounds bounds) {
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

    public static GameRegion deserialize(CompoundTag tag) {
        String marker = tag.getString("marker");
        if (marker.startsWith("minecraft:")) {
            // TODO: temporary fix for legacy map files
            marker = marker.substring("minecraft:".length());
        }
        return new GameRegion(marker, BlockBounds.deserialize(tag));
    }
}
