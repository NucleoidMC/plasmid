package xyz.nucleoid.plasmid.game.stats;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Map;
import java.util.UUID;

/**
 * Wrapper containing a map players' {@link StatisticBundle}s
 */
public class GameStatisticBundle {
    private final Object2ObjectMap<UUID, StatisticBundle> players = new Object2ObjectOpenHashMap<>();
    private final StatisticBundle global = new StatisticBundle();

    public StatisticBundle getPlayer(PlayerRef player) {
        return this.getPlayer(player.getId());
    }

    public StatisticBundle getPlayer(ServerPlayerEntity player) {
        return this.getPlayer(player.getUuid());
    }

    public StatisticBundle getPlayer(UUID uuid) {
        return this.players.computeIfAbsent(uuid, __ -> new StatisticBundle());
    }

    public StatisticBundle getGlobal() {
        return this.global;
    }

    public boolean isEmpty() {
        return this.global.isEmpty()
                && (this.players.isEmpty() || this.players.values().stream().allMatch(StatisticBundle::isEmpty));
    }

    public JsonObject encodeBundle() {
        JsonObject obj = new JsonObject();
        JsonObject players = new JsonObject();
        for (Map.Entry<UUID, StatisticBundle> entry : this.players.entrySet()) {
            players.add(entry.getKey().toString(), entry.getValue().encodeBundle());
        }

        obj.add("players", players);

        if (!this.global.isEmpty()) {
            obj.add("global", this.global.encodeBundle());
        }
        return obj;
    }
}
