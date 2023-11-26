package xyz.nucleoid.plasmid.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.EitherCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public final class TinyRegistry<T> implements Codec<T> {
    private final BiMap<Identifier, T> map = HashBiMap.create();
    @Nullable
    private final TagGroupLoader<T> tagGroupLoader;
    private Map<Identifier, Collection<T>> tags = Map.of();
    private Map<Identifier, List<TagGroupLoader.TrackedEntry>> tagsDefinitions = Map.of();

    private TinyRegistry(@Nullable String name) {
        if (name != null) {
            this.tagGroupLoader = new TagGroupLoader<>(this::getOptional, "tags/" + name);
        } else {
            this.tagGroupLoader = null;
        }
    }

    public static <T> TinyRegistry<T> create() {
        return new TinyRegistry<>(null);
    }

    public static <T> TinyRegistry<T> create(String name) {
        return new TinyRegistry<>(name);
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

    public Optional<T> getOptional(Identifier identifier) {
        return Optional.ofNullable(get(identifier));
    }

    @Nullable
    public Identifier getIdentifier(T value) {
        return this.map.inverse().get(value);
    }

    public boolean containsKey(Identifier identifier) {
        return this.map.containsKey(identifier);
    }

    public boolean containsTag(Identifier identifier) {
        return this.map.containsKey(identifier);
    }

    public Collection<T> getTag(Identifier identifier) {
        var tag = this.tags.get(identifier);
        if (tag == null) {
            return List.of();
        }
        return tag;
    }

    @Nullable
    public Collection<T> getTagOrNull(Identifier identifier) {
        return this.tags.get(identifier);
    }

    public Codec<TinyEntry<EntryKey<?>, Collection<T>>> getEntryCodec() {
        return new EntryCodec();
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

    public void loadTags(ResourceManager manager) {
        this.loadTagsDefinitions(manager);
        this.finalizeTags();
    }

    public void loadTagsDefinitions(ResourceManager manager) {
        if (this.tagGroupLoader == null) {
            return;
        }
        this.tags = Map.of();
        this.tagsDefinitions = this.tagGroupLoader.loadTags(manager);
    }

    public void finalizeTags() {
        if (this.tagsDefinitions == null || this.tagGroupLoader == null) {
            return;
        }
        this.tags = this.tagGroupLoader.buildGroup(this.tagsDefinitions);
    }

    public record EntryKey<T>(T key, Type<T> type) {

        public record Type<T>() {
            public static final Type<Identifier> ENTRY = new Type<>();
            public static final Type<Identifier> TAG = new Type<>();
            public static final Type<Collection<EntryKey<?>>> LIST = new Type<>();

            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }
        }
    }

    private class EntryCodec implements Codec<TinyEntry<EntryKey<?>, Collection<T>>> {
        //private Codec<List<T>> self
        @Override
        public <I> DataResult<Pair<TinyEntry<EntryKey<?>, Collection<T>>, I>> decode(DynamicOps<I> ops, I input) {
            var key = Codec.STRING.decode(ops, input);
            if (key.result().isPresent()) {
                var id = key.result().get().getFirst();
                if (id.startsWith("#")) {
                    return Identifier.CODEC.decode(ops, ops.createString(id.substring(1)))
                            .map(p -> p.mapFirst(idx -> TinyEntry.of(new EntryKey<>(idx, EntryKey.Type.TAG),
                                    (x) -> TinyRegistry.this.getTagOrNull((Identifier) x.key()))));
                } else {
                    return Identifier.CODEC.decode(ops, ops.createString(id))
                            .map(p -> p.mapFirst(idx -> TinyEntry.of(new EntryKey<>(idx, EntryKey.Type.ENTRY),
                                    (x) -> {
                                        var y = TinyRegistry.this.get((Identifier) x.key());
                                        if (y == null) {
                                            return null;
                                        }
                                        return List.of(y);
                                    })));
                }
            }

            var list = Codec.list(this).decode(ops, input).map(x ->
                    x.mapFirst(z -> TinyEntry.merge(z,
                            y -> new EntryKey<>(y, EntryKey.Type.LIST), y -> {
                        var merge = new ArrayList<T>();
                        for (var v : y) {
                            merge.addAll(v);
                        }
                        return merge;
                    })));

            //noinspection unchecked
            return (DataResult<Pair<TinyEntry<EntryKey<?>, Collection<T>>, I>>) (Object) list;
        }

        @Override
        public <I> DataResult<I> encode(TinyEntry<EntryKey<?>, Collection<T>> input, DynamicOps<I> ops, I prefix) {
            if (input.id().type == EntryKey.Type.ENTRY) {
                return ops.mergeToPrimitive(prefix, ops.createString(input.id().key.toString()));
            } else if (input.id().type == EntryKey.Type.TAG) {
                return ops.mergeToPrimitive(prefix, ops.createString("#" + input.id().key.toString()));
            } else {
                var list = ((Collection<EntryKey<Identifier>>) input.id().key)
                        .stream().map(x -> ops.createString((x.type == EntryKey.Type.TAG ? "#" : "") + x.key.toString()) ).toList();
                return ops.mergeToList(prefix, list);
            }
        }
    }
}
