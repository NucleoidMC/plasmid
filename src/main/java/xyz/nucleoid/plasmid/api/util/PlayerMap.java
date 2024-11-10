package xyz.nucleoid.plasmid.api.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.player.PlayerMapImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This is a map that provides extra utility methods allowing you to easily use this map with {@link ServerPlayerEntity}, as with the backing PlayerRef.
 * @param <T> the type of stored values
 */
public interface PlayerMap<T> extends Map<PlayerRef, T> {
    static <T> PlayerMap<T> of(Map<PlayerRef, T> map) {
        return new PlayerMapImpl<>(map);
    }

    static <T> PlayerMap<T> createHashMap() {
        return new PlayerMapImpl<>(new HashMap<>());
    }

    static <T> PlayerMap<T> createHashMap(int size) {
        return new PlayerMapImpl<>(new HashMap<>(size));
    }
    boolean containsKey(ServerPlayerEntity key);
    T get(ServerPlayerEntity key);
    @Nullable
    T put(ServerPlayerEntity key, T value);
    T remove(ServerPlayerEntity key);

    void forEachPlayer(GameSpace gameSpace, BiConsumer<ServerPlayerEntity, T> consumer);
    void forEachPlayer(ServerWorld world, BiConsumer<ServerPlayerEntity, T> consumer);
    void forEachPlayer(MinecraftServer server, BiConsumer<ServerPlayerEntity, T> consumer);

    Map<PlayerRef, T> getBackingMap();
}
