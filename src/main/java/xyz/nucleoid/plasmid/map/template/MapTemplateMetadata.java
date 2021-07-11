package xyz.nucleoid.plasmid.map.template;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.map.BlockBounds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents the non-world data of a MapTemplate that can be used to control additional game logic, but has no impact
 * in what blocks or entities are placed in the world.
 *
 * This includes {@link TemplateRegion} and arbitrary {@link NbtCompound} attached data
 */
public final class MapTemplateMetadata {
    final List<TemplateRegion> regions = new ArrayList<>();
    NbtCompound data = new NbtCompound();

    /**
     * Adds a region with the given marker tag and bounds.
     * Note that markers are not unique: multiple regions can be assigned the same marker!
     *
     * @param marker the marker tag to index this region by
     * @param bounds the area over which this region exists
     * @return the added region
     */
    public TemplateRegion addRegion(String marker, BlockBounds bounds) {
        return this.addRegion(marker, bounds, new NbtCompound());
    }

    /**
     * Adds a region with the given marker tag, bounds, and {@link NbtCompound} attached data.
     * Note that markers are not unique: multiple regions can be assigned the same marker!
     *
     * @param marker the marker tag to index this region by
     * @param bounds the area over which this region exists
     * @param tag arbitrary attached data
     * @return the added region
     */
    public TemplateRegion addRegion(String marker, BlockBounds bounds, NbtCompound tag) {
        TemplateRegion region = new TemplateRegion(marker, bounds, tag);
        this.regions.add(region);
        return region;
    }

    public void addRegion(TemplateRegion region) {
        this.regions.add(region);
    }

    /**
     * Queries all regions within this map that match the given marker.
     *
     * @param marker the marker to query for
     * @return a stream of regions that match the query
     */
    public Stream<TemplateRegion> getRegions(String marker) {
        return this.regions.stream()
                .filter(region -> region.getMarker().equals(marker));
    }

    /**
     * Queries all region bounds within this map that match the given marker.
     *
     * @param marker the marker to query for
     * @return a stream of region bounds that match the query
     */
    public Stream<BlockBounds> getRegionBounds(String marker) {
        return this.getRegions(marker)
                .map(TemplateRegion::getBounds);
    }

    /**
     * Queries a single region within this map that matches the given marker.
     * This method gives no guarantees around which will be returned in the case of duplicates.
     *
     * @param marker the marker to query for
     * @return the region that matches the query
     */
    @Nullable
    public TemplateRegion getFirstRegion(String marker) {
        return this.getRegions(marker).findFirst().orElse(null);
    }

    /**
     * Queries a single region within this map that matches the given marker.
     * This method gives no guarantees around which will be returned in the case of duplicates.
     *
     * @param marker the marker to query for
     * @return the region bounds that matches the query
     */
    @Nullable
    public BlockBounds getFirstRegionBounds(String marker) {
        return this.getRegionBounds(marker).findFirst().orElse(null);
    }

    public Collection<TemplateRegion> getRegions() {
        return this.regions;
    }

    /**
     * Sets the arbitrary data of the map.
     *
     * @param data the data as a compound tag
     */
    public void setData(NbtCompound data) {
        this.data = data;
    }

    /**
     * Gets the arbitrary data of the map.
     *
     * @return the data as a compound tag
     */
    public NbtCompound getData() {
        return this.data;
    }
}
