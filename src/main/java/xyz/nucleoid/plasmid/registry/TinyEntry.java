package xyz.nucleoid.plasmid.registry;

import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.config.GameConfig;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TinyEntry<K, T> {
    private final Function<K, T> getter;
    private final K id;

    private TinyEntry(K K, Function<K, T> getter) {
        this.id = K;
        this.getter = getter;
    }
    public static <K, T> Codec<TinyEntry<K, T>> codec(Codec<K> key, Function<K, T> getter) {
        return key.xmap(x -> new TinyEntry<>(x, getter), TinyEntry::id);
    }

    public static <T> Codec<TinyEntry<Identifier, T>> codec(TinyRegistry<T> registry) {
        return codec(Identifier.CODEC, registry::get);
    }

    public static <T> Codec<TinyEntry<Identifier, Collection<T>>> tagCodec(TinyRegistry<T> registry) {
        return codec(Identifier.CODEC,registry::getTag);
    }

    public static <K, T> TinyEntry<K, T> of(K K, Function<K, T> getter) {
        return new TinyEntry<>(K, getter);
    }

    public static <T> TinyEntry<Identifier, T> of(Identifier K, TinyRegistry<T> registry) {
        return of(K, registry::get);
    }

    public static <T> TinyEntry<Identifier, Collection<T>> ofTag(Identifier K, TinyRegistry<T> registry) {
        return of(K, registry::getTag);
    }

    public static <K, T, K2, T2> TinyEntry<K, T> merge(Collection<TinyEntry<K2, T2>> collection,
                                                       Function<Collection<K2>, K> keyFunc,
                                                       Function<Collection<T2>, T> valueFunc
    ) {
        var keys = new ArrayList<K2>();
        for (var x : collection) {
            keys.add(x.id);
        }

        return new TinyEntry<>(keyFunc.apply(keys), k -> {
            var finalValues = new ArrayList<T2>();

            for (var entry : collection) {
                var value = entry.get();
                if (value == null) {
                    return null;
                }
                finalValues.add(value);
            }

            return valueFunc.apply(finalValues);
        });
    }

    public K id() {
        return this.id;
    }

    @Nullable
    public T get() {
        return this.getter.apply(this.id);
    }

    public Optional<T> optional() {
        return Optional.ofNullable(this.get());
    }

    public void ifPresent(Consumer<T> consumer) {
        var x = this.get();
        if (x != null) {
            consumer.accept(x);
        }
    }

    public void ifPresent(Consumer<T> consumer, Runnable otherwise) {
        var x = this.get();
        if (x != null) {
            consumer.accept(x);
        } else {
            otherwise.run();
        }
    }

    public T orElse(Supplier<? extends T> supplier) {
        var x = this.get();
        if (x == null) {
            return supplier.get();
        }
        return x;
    }
}
