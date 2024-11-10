package xyz.nucleoid.plasmid.impl.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.util.PlayerMap;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.*;
import java.util.function.BiConsumer;

public record PlayerMapImpl<T>(Map<PlayerRef, T> map) implements PlayerMap<T> {
    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public T get(Object key) {
        return this.map.get(key);
    }

    @Nullable
    @Override
    public T put(PlayerRef key, T value) {
        return this.map.put(key, value);
    }

    @Override
    public T remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public void putAll(@NotNull Map<? extends PlayerRef, ? extends T> m) {
        this.map.putAll(m);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @NotNull
    @Override
    public Set<PlayerRef> keySet() {
        return this.map.keySet();
    }

    @NotNull
    @Override
    public Collection<T> values() {
        return this.map.values();
    }

    @NotNull
    @Override
    public Set<Entry<PlayerRef, T>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public boolean containsKey(ServerPlayerEntity key) {
        return this.containsKey(PlayerRef.of(key));
    }

    @Override
    public T get(ServerPlayerEntity key) {
        return this.get(PlayerRef.of(key));
    }

    @Override
    public @Nullable T put(ServerPlayerEntity key, T value) {
        return this.put(PlayerRef.of(key), value);
    }

    @Override
    public T remove(ServerPlayerEntity key) {
        return this.remove(PlayerRef.of(key));
    }

    @Override
    public void forEachPlayer(GameSpace gameSpace, BiConsumer<ServerPlayerEntity, T> consumer) {
        this.forEach((ref, value) -> {
            var player = ref.getEntity(gameSpace);
            if (player != null) {
                consumer.accept(player, value);
            }
        });
    }

    @Override
    public void forEachPlayer(ServerWorld world, BiConsumer<ServerPlayerEntity, T> consumer) {
        this.forEach((ref, value) -> {
            var player = ref.getEntity(world);
            if (player != null) {
                consumer.accept(player, value);
            }
        });
    }

    @Override
    public void forEachPlayer(MinecraftServer server, BiConsumer<ServerPlayerEntity, T> consumer) {
        this.forEach((ref, value) -> {
            var player = ref.getEntity(server);
            if (player != null) {
                consumer.accept(player, value);
            }
        });
    }

    @Override
    public Map<PlayerRef, T> getBackingMap() {
        return this.map;
    }
}
