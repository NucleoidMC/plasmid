package net.gegy1000.plasmid.game.map;

import net.gegy1000.plasmid.Plasmid;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class StagingMapManager extends PersistentState {
    public static final String KEY = Plasmid.ID + ":staging_maps";

    private final ServerWorld world;
    private final Map<Identifier, StagingMap> stagingMaps = new HashMap<>();

    private StagingMapManager(ServerWorld world) {
        super(KEY);
        this.world = world;
    }

    public static StagingMapManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new StagingMapManager(world), KEY);
    }

    public StagingMap add(Identifier identifier, BlockBounds bounds) {
        StagingMap map = new StagingMap(this.world, identifier, bounds);
        this.stagingMaps.put(identifier, map);
        this.setDirty(true);
        return map;
    }

    @Nullable
    public StagingMap get(Identifier identifier) {
        return this.stagingMaps.get(identifier);
    }

    public Set<Identifier> getStagingMapKeys() {
        return this.stagingMaps.keySet();
    }

    public Collection<StagingMap> getStagingMaps() {
        return this.stagingMaps.values();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.stagingMaps.clear();

        for (String key : tag.getKeys()) {
            Identifier identifier = new Identifier(key);
            CompoundTag root = tag.getCompound(key);
            this.stagingMaps.put(identifier, StagingMap.deserialize(this.world, root));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        for (Map.Entry<Identifier, StagingMap> entry : this.stagingMaps.entrySet()) {
            String key = entry.getKey().toString();
            tag.put(key, entry.getValue().serialize(new CompoundTag()));
        }
        return tag;
    }
}
