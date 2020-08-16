package xyz.nucleoid.plasmid.game.map.template;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class StagingMapManager extends PersistentState {
    public static final String KEY = Plasmid.ID + ":staging_maps";

    private final ServerWorld world;
    private final Map<Identifier, StagingMapTemplate> stagingMaps = new HashMap<>();

    private StagingMapManager(ServerWorld world) {
        super(KEY);
        this.world = world;
    }

    public static StagingMapManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new StagingMapManager(world), KEY);
    }

    public StagingMapTemplate add(Identifier identifier, BlockBounds bounds) {
        StagingMapTemplate map = new StagingMapTemplate(this.world, identifier, bounds);
        this.stagingMaps.put(identifier, map);
        this.setDirty(true);
        return map;
    }

    @Nullable
    public StagingMapTemplate get(Identifier identifier) {
        return this.stagingMaps.get(identifier);
    }

    public Set<Identifier> getStagingMapKeys() {
        return this.stagingMaps.keySet();
    }

    public Collection<StagingMapTemplate> getStagingMaps() {
        return this.stagingMaps.values();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.stagingMaps.clear();

        for (String key : tag.getKeys()) {
            Identifier identifier = new Identifier(key);
            CompoundTag root = tag.getCompound(key);
            this.stagingMaps.put(identifier, StagingMapTemplate.deserialize(this.world, root));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        for (Map.Entry<Identifier, StagingMapTemplate> entry : this.stagingMaps.entrySet()) {
            String key = entry.getKey().toString();
            tag.put(key, entry.getValue().serialize(new CompoundTag()));
        }
        return tag;
    }
}
