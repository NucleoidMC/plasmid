package net.gegy1000.plasmid.game;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.gegy1000.plasmid.game.event.EventType;
import net.gegy1000.plasmid.game.event.GameCloseListener;
import net.gegy1000.plasmid.game.event.GameOpenListener;
import net.gegy1000.plasmid.game.event.GameTickListener;
import net.gegy1000.plasmid.game.event.OfferPlayerListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerRemoveListener;
import net.gegy1000.plasmid.game.event.RequestStartListener;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.player.PlayerSnapshot;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.GameRuleSet;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public final class Game {
    private final Map<EventType<?>, Object> invokers;

    private final Map<UUID, PlayerSnapshot> players = new HashMap<>();

    private final GameMap map;
    private final ServerWorld world;
    private final GameRuleSet rules;

    private Game(GameMap map, GameRuleSet rules, Map<EventType<?>, Object> invokers) {
        this.map = map;
        this.world = map.getWorld();
        this.rules = rules;

        this.invokers = invokers;

        this.invoker(GameOpenListener.EVENT).open(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void tick() {
        this.invoker(GameTickListener.EVENT).tick(this);
    }

    public void copyPlayersFrom(Game game) {
        ServerWorld world = game.getWorld();

        for (Map.Entry<UUID, PlayerSnapshot> entry : game.players.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerSnapshot snapshot = entry.getValue();

            this.players.put(uuid, snapshot);

            ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(uuid);
            if (player != null) {
                this.notifyAddPlayer(player);
            }
        }
    }

    public StartResult requestStart() {
        return this.invoker(RequestStartListener.EVENT).requestStart(this);
    }

    public JoinResult offerPlayer(ServerPlayerEntity player) {
        JoinResult result = this.invoker(OfferPlayerListener.EVENT).offerPlayer(this, player);
        if (result.isErr()) {
            return result;
        }

        if (this.addPlayer(player)) {
            return JoinResult.ok();
        } else {
            return JoinResult.alreadyJoined();
        }
    }

    public boolean addPlayer(ServerPlayerEntity player) {
        if (!this.players.containsKey(player.getUuid())) {
            this.players.put(player.getUuid(), PlayerSnapshot.take(player));
            this.notifyAddPlayer(player);

            return true;
        }

        return false;
    }

    private void notifyAddPlayer(ServerPlayerEntity player) {
        this.invoker(PlayerAddListener.EVENT).onAddPlayer(this, player);
    }

    public void removePlayer(ServerPlayerEntity player) {
        PlayerSnapshot snapshot = this.players.remove(player.getUuid());

        if (snapshot != null) {
            this.invoker(PlayerRemoveListener.EVENT).onRemovePlayer(this, player);

            snapshot.restore(player);
        }
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public Set<UUID> getPlayerIds() {
        return this.players.keySet();
    }

    public Stream<ServerPlayerEntity> onlinePlayers() {
        return this.players.keySet().stream()
                .map(uuid -> (ServerPlayerEntity) this.world.getPlayerByUuid(uuid))
                .filter(Objects::nonNull);
    }

    public boolean containsPlayer(PlayerEntity player) {
        return this.players.containsKey(player.getUuid());
    }

    public boolean containsPos(BlockPos pos) {
        return this.map.getBounds().contains(pos);
    }

    public void close() {
        this.invoker(GameCloseListener.EVENT).close(this);

        for (Map.Entry<UUID, PlayerSnapshot> entry : this.players.entrySet()) {
            // TODO: restoring offline players
            ServerPlayerEntity player = (ServerPlayerEntity) this.world.getPlayerByUuid(entry.getKey());
            if (player != null) {
                entry.getValue().restore(player);
            }
        }

        this.map.delete();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> T invoker(EventType<T> event) {
        return (T) this.invokers.computeIfAbsent(event, EventType::createEmpty);
    }

    public GameMap getMap() {
        return this.map;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public RuleResult testRule(GameRule rule) {
        return this.rules.test(rule);
    }

    public static class Builder {
        private final Multimap<EventType<?>, Object> listeners = HashMultimap.create();
        private final GameRuleSet rules = new GameRuleSet();
        private GameMap map;

        private Builder() {
        }

        public Builder setMap(GameMap map) {
            this.map = map;
            return this;
        }

        public Builder setRule(GameRule rule, RuleResult result) {
            this.rules.put(rule, result);
            return this;
        }

        public <T> Builder on(EventType<T> type, T listener) {
            this.listeners.put(type, listener);
            return this;
        }

        public Game build() {
            GameMap map = Preconditions.checkNotNull(this.map, "map not set");

            Map<EventType<?>, Object> invokers = new HashMap<>();

            for (EventType<?> event : this.listeners.keySet()) {
                Object combined = event.combineUnchecked(this.listeners.get(event));
                invokers.put(event, combined);
            }

            return new Game(map, this.rules, invokers);
        }
    }
}
