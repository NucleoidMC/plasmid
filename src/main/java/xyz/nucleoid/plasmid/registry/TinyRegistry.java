package xyz.nucleoid.plasmid.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public final class TinyRegistry<T> implements Codec<T> {
    private final BiMap<Identifier, T> map = HashBiMap.create();

    private TinyRegistry() {
    }

    public static <T> TinyRegistry<T> create() {
        return new TinyRegistry<>();
    }

    public void clear() {
        this.map.clear();
    }

    public void register(Identifier identifier, T value) {
        this.map.put(identifier, value);
    }

    @Nullable
    public T get(Identifier identifier) {
        return this.map.get(identifier);
    }

    @Nullable
    public Identifier getIdentifier(T value) {
        return this.map.inverse().get(value);
    }

    public boolean containsKey(Identifier identifier) {
        return this.map.containsKey(identifier);
    }

    @Override
    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U input) {
        return Identifier.CODEC.decode(ops, input)
                .flatMap(pair -> {
                    if (!this.containsKey(pair.getFirst())) {
                        return DataResult.error(() -> "Unknown registry key: " + pair.getFirst());
                    }
                    return DataResult.success(pair.mapFirst(this::get));
                });
    }

    @Override
    public <U> DataResult<U> encode(T input, DynamicOps<U> ops, U prefix) {
        var identifier = this.getIdentifier(input);
        if (identifier == null) {
            return DataResult.error(() -> "Unknown registry element " + input);
        }
        return ops.mergeToPrimitive(prefix, ops.createString(identifier.toString()));
    }

    public Set<Identifier> keySet() {
        return this.map.keySet();
    }

    public Collection<T> values() {
        return this.map.values();
    }
}
