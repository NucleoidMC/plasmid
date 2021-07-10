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
    }

    public static ServerStorageManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(ServerStorageManager::readNbt, ServerStorageManager::new, KEY);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var storageList = new NbtList();
        ServerStorage.STORAGES.forEach((key, value) -> {
            var storageTag = value.toTag();
            storageTag.putString("id", key.toString());
            storageList.add(storageTag);
        });
        nbt.put("storages", storageList);
        return nbt;
    }

    private static ServerStorageManager readNbt(NbtCompound nbt) {
        loaded = true;

        var storageTags = nbt.getList("storages", NbtType.COMPOUND);

        for (int i = 0; i < storageTags.size(); i++) {
            var storageTag = storageTags.getCompound(i);
            var storage = ServerStorage.STORAGES.get(new Identifier(storageTag.getString("id")));
            if (storage != null) {
                storage.fromTag(storageTag);
            }
        }

        return new ServerStorageManager();
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
