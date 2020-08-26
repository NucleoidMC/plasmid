package xyz.nucleoid.plasmid.storage;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import xyz.nucleoid.plasmid.Plasmid;

public final class ServerStorageManager extends PersistentState {
    private static final String KEY = Plasmid.ID + "_storage";

    ServerStorageManager() {
        super(KEY);
    }

    public static ServerStorageManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new ServerStorageManager(), KEY);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag storageList = new ListTag();
        ServerStorage.STORAGES.entrySet().forEach((entry) -> {
            CompoundTag storageTag = entry.getValue().toTag();
            storageTag.putString("id", entry.getKey().toString());
            storageList.add(storageTag);
        });
        tag.put("storages", storageList);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ListTag storageTags = tag.getList("storages", NbtType.COMPOUND);

        for (int i = 0; i < storageTags.size(); i++) {
            CompoundTag storageTag = storageTags.getCompound(i);
            ServerStorage storage = ServerStorage.STORAGES.get(new Identifier(storageTag.getString("id")));
            if (storage != null) {
                storage.fromTag(storageTag);
            }
        }
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
