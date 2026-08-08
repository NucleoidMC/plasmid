package xyz.nucleoid.plasmid.impl.menu;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;

import java.util.Map;

/**
 * One entry instance per registry definition.
 * <p>
 * Entries carry live state such as a pending game space or a polled player count, so two menus listing
 * the same definition must share the instance. Without this, "join the game in progress" would open a
 * second one.
 * <p>
 * Keyed by identity: registry values are themselves singletons for the lifetime of a datapack load, and the
 * cache is dropped whenever they are reloaded.
 */
public final class GameMenuEntries {
    private static final Map<GameMenuEntryConfig, GameMenuEntry> CACHE = new Reference2ObjectOpenHashMap<>();

    private GameMenuEntries() {
    }

    public static GameMenuEntry of(GameMenuEntryConfig config) {
        return CACHE.computeIfAbsent(config, GameMenuEntryConfig::createEntry);
    }

    public static void clear() {
        CACHE.clear();
    }
}
