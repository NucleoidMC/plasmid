package xyz.nucleoid.plasmid.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Implemented in classes that will store NBT data on the server.
 * This interface is useful for storing data about games, e.g. player scores.
 *
 * @see {@link net.minecraft.world.PersistentState}
 */
public interface ServerStorage {
    Map<Identifier, ServerStorage> STORAGES = new HashMap<>();

    /**
     * Adds a {@link ServerStorage} to the {@link #STORAGES} map and returns the {@link ServerStorage}.
     * Use this to have your {@link ServerStorage} be saved and loaded.
     * @param key - The id of the storage.
     * @param storage - The {@link ServerStorage} to add to the {@link #STORAGES} map.
     * @param <S> - The type of {@link ServerStorage}.
     * @return - The supplied {@link ServerStorage}.
     */
    static <S extends ServerStorage> S createStorage(Identifier key, S storage) {
        if (ServerStorageManager.isLoaded()) {
            throw new IllegalStateException(String.format("Server Storage with id %s was created after Server Storage Manager loaded!", key));
        }
        STORAGES.put(key, storage);
        return storage;
    }

    /**
     * Called when saving this {@link ServerStorage} to NBT.
     * @return - The serialized NBT data of this {@link ServerStorage}.
     */
    CompoundTag toTag();

    /**
     * Called when loading the saved NBT data for this {@link ServerStorage}.
     * @param tag - The deserialized NBT data of this {@link ServerStorage}.
     */
    void fromTag(CompoundTag tag);
}
