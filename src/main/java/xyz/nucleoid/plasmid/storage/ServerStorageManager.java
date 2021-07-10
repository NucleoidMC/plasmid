package xyz.nucleoid.plasmid.storage;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import xyz.nucleoid.plasmid.Plasmid;

public final class ServerStorageManager extends PersistentState {
    private static final String KEY = Plasmid.ID + "_storage";
    private static boolean loaded = false;

    ServerStorageManager() {
        super(KEY);
    }

    public static ServerStorageManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(ServerStorageManager::new, KEY);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList storageList = new NbtList();
        ServerStorage.STORAGES.entrySet().forEach((entry) -> {
            NbtCompound storageTag = entry.getValue().toTag();
            storageTag.putString("id", entry.getKey().toString());
            storageList.add(storageTag);
        });
        tag.put("storages", storageList);
        return tag;
    }

    @Override
    public void fromTag(NbtCompound tag) {
        loaded = true;
        NbtList storageTags = tag.getList("storages", NbtType.COMPOUND);

        for (int i = 0; i < storageTags.size(); i++) {
            NbtCompound storageTag = storageTags.getCompound(i);
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
