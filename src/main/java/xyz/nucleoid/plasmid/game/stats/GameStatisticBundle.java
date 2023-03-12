package xyz.nucleoid.plasmid.game.stats;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Wrapper containing a map players' {@link StatisticMap}s
 * Similarly to {@link StatisticKey}s, {@link GameStatisticBundle}s should provide a translation
 * key for their namespace in the form <code>statistic.bundle.[namespace]</code>
 */
public class GameStatisticBundle {
    private static final String NAMESPACE_ERROR_MESSAGE = "Bundle namespaces can only contain [a-zA-Z0-9_]";
    public static final Codec<String> NAMESPACE_CODEC = Codec.STRING.comapFlatMap(
            string -> {
                for (char c : string.toCharArray()) {
                    if (!validateNamespaceChar(c)) {
                        return DataResult.error(() -> NAMESPACE_ERROR_MESSAGE);
                    }
                }
                return DataResult.success(string);
            },
            Function.identity()
    );

    private final Object2ObjectMap<UUID, StatisticMap> players = new Object2ObjectOpenHashMap<>();
    private final StatisticMap global = new StatisticMap();

    public StatisticMap forPlayer(PlayerRef player) {
        return this.forPlayer(player.id());
    }

    public StatisticMap forPlayer(ServerPlayerEntity player) {
        return this.forPlayer(player.getUuid());
    }

    public StatisticMap forPlayer(UUID uuid) {
        return this.players.computeIfAbsent(uuid, __ -> new StatisticMap());
    }

    public StatisticMap global() {
        return this.global;
    }

    public boolean isEmpty() {
        return this.global.isEmpty()
                && (this.players.isEmpty() || this.players.values().stream().allMatch(StatisticMap::isEmpty));
    }

    public JsonObject encode() {
        JsonObject obj = new JsonObject();
        JsonObject players = new JsonObject();
        for (Map.Entry<UUID, StatisticMap> entry : this.players.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                players.add(entry.getKey().toString(), entry.getValue().encode());
            }
        }

        obj.add("players", players);

        if (!this.global.isEmpty()) {
            obj.add("global", this.global.encode());
        }

        return obj;
    }

    public static void validateNamespace(String namespace) {
        for (char c : namespace.toCharArray()) {
            if (!validateNamespaceChar(c)) {
                throw new IllegalArgumentException(NAMESPACE_ERROR_MESSAGE);
            }
        }
    }

    public static String getTranslationKey(String namespace) {
        return "statistic.bundle." + namespace;
    }

    private static boolean validateNamespaceChar(char c) {
        return (c >= 'a' && c <= 'z') // a-z
                || (c >= 'A' && c <= 'Z') // A-Z
                || (c >= '0' && c <= '9') // 0-9
                || c == '_'; // _
    }
}
