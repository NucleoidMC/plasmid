package xyz.nucleoid.plasmid.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;

import java.util.function.BiConsumer;

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
