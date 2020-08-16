package xyz.nucleoid.plasmid.game.player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class TeamAllocator<K, V> {
    private final List<K> keys;

    private final List<Pair<K, V>> withRequest = new ArrayList<>();
    private final List<V> withoutRequest = new ArrayList<>();

    public TeamAllocator(Collection<K> keys) {
        this.keys = new ArrayList<>(keys);
        Collections.shuffle(this.keys);
    }

    public void add(V value, @Nullable K request) {
        if (request != null) {
            this.withRequest.add(Pair.of(request, value));
        } else {
            this.withoutRequest.add(value);
        }
    }

    // TODO: we don't ensure that every team has players
    public Multimap<K, V> build() {
        Multimap<K, V> map = HashMultimap.create();

        Collections.shuffle(this.withRequest);
        Collections.shuffle(this.withoutRequest);

        int totalKeys = this.keys.size();
        int totalValues = this.withRequest.size() + this.withoutRequest.size();

        int keyCapacity = (totalValues + totalKeys - 1) / totalKeys;

        for (Pair<K, V> request : this.withRequest) {
            K key = request.getFirst();
            V value = request.getSecond();

            if (!this.tryInsertInto(map, keyCapacity, key, value)) {
                this.insertIntoSmallest(map, value);
            }
        }

        for (V value : this.withoutRequest) {
            this.insertIntoSmallest(map, value);
        }

        return map;
    }

    private void insertIntoSmallest(Multimap<K, V> map, V value) {
        Collection<V> smallestBucket = null;

        for (K key : this.keys) {
            Collection<V> bucket = map.get(key);
            if (smallestBucket == null || bucket.size() < smallestBucket.size()) {
                smallestBucket = bucket;
            }
        }

        if (smallestBucket == null) {
            throw new Error("no available buckets");
        }

        smallestBucket.add(value);
    }

    private boolean tryInsertInto(Multimap<K, V> map, int keyCapacity, K key, V value) {
        Collection<V> bucket = map.get(key);
        if (bucket.size() < keyCapacity) {
            bucket.add(value);
            return true;
        }
        return false;
    }
}
