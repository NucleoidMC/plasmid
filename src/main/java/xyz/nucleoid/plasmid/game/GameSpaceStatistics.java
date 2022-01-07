package xyz.nucleoid.plasmid.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

/**
 * Holds the {@link GameStatisticBundle} instances associated with a {@link GameSpace} instance.
 */
public final class GameSpaceStatistics {
    private final Object2ObjectMap<String, GameStatisticBundle> statistics = new Object2ObjectOpenHashMap<>();

    /**
     * Note: bundle namespaces can only contain the characters a-zA-Z0-9_
     *
     * @param namespace The statistic namespace to get a bundle for
     * @return the {@link GameStatisticBundle} for the given namespace
     */
    public GameStatisticBundle bundle(String namespace) {
        GameStatisticBundle.validateNamespace(namespace); // Will throw an exception if validation fails.
        return this.statistics.computeIfAbsent(namespace, $ -> new GameStatisticBundle());
    }

    /**
     * Note: bundle namespaces can only contain the characters a-zA-Z0-9_
     *
     * @param namespace The statistic namespace to get a bundle for, or {@code null} for an empty bundle
     * @return the {@link GameStatisticBundle} for the given namespace, or {@linkplain GameStatisticBundle#EMPTY an empty bundle}
     * @see GameStatisticBundle#bundle(String)
     */
    public GameStatisticBundle bundleOrEmpty(@Nullable String namespace) {
        return namespace == null ? GameStatisticBundle.EMPTY : this.bundle(namespace);
    }

    /**
     * @param consumer Will be called for every non-empty {@link GameStatisticBundle} in this {@link GameSpace}
     */
    public void visitAll(BiConsumer<String, GameStatisticBundle> consumer) {
        for (var entry : this.statistics.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }
    }
}
