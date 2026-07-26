package xyz.nucleoid.plasmid.api.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.player.PlayerMapImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This is a map that provides extra utility methods allowing you to easily use this map with {@link ServerPlayer}, as with the backing PlayerRef.
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
    boolean containsKey(ServerPlayer key);
    T get(ServerPlayer key);
    @Nullable
    T put(ServerPlayer key, T value);
    T remove(ServerPlayer key);

    void forEachPlayer(GameSpace gameSpace, BiConsumer<ServerPlayer, T> consumer);
    void forEachPlayer(ServerLevel world, BiConsumer<ServerPlayer, T> consumer);
    void forEachPlayer(MinecraftServer server, BiConsumer<ServerPlayer, T> consumer);

    Map<PlayerRef, T> getBackingMap();
}
