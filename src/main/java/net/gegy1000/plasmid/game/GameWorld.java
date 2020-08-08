package net.gegy1000.plasmid.game;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.gegy1000.plasmid.game.event.EventType;
import net.gegy1000.plasmid.game.event.GameCloseListener;
import net.gegy1000.plasmid.game.event.GameOpenListener;
import net.gegy1000.plasmid.game.event.GameTickListener;
import net.gegy1000.plasmid.game.event.OfferPlayerListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerRemoveListener;
import net.gegy1000.plasmid.game.event.RequestStartListener;
import net.gegy1000.plasmid.game.player.JoinResult;
import net.gegy1000.plasmid.game.player.PlayerSnapshot;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.GameRuleSet;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class GameWorld {
    private final GameWorldState state;
    private final ServerWorld world;

    private Map<EventType<?>, Object> invokers = new HashMap<>();
    private GameRuleSet rules = GameRuleSet.empty();

    private final Reference2ObjectMap<ServerPlayerEntity, PlayerSnapshot> players = new Reference2ObjectOpenHashMap<>();

    private boolean closed;

    GameWorld(GameWorldState state) {
        this.state = state;
        this.world = state.world;
    }

    @Nullable
    public static GameWorld forWorld(World world) {
        GameWorldState worldState = GameWorldState.forWorld(world);
        if (worldState != null) {
            return worldState.getOpenWorld();
        }
        return null;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public void newGame(Consumer<Game> builder) {
        Game game = new Game();
        builder.accept(game);

        this.setGame(game);
    }

    public void setGame(Game game) {
        this.closeGame();

        this.invokers = game.getInvokers();
        this.rules = game.getRules();

        this.invoker(GameOpenListener.EVENT).onOpen();

        for (ServerPlayerEntity player : this.players.keySet()) {
            this.invoker(PlayerAddListener.EVENT).onAddPlayer(player);
        }
    }

    public void closeGame() {
        this.invoker(GameCloseListener.EVENT).onClose();
        this.invokers = new HashMap<>();
        this.rules = GameRuleSet.empty();
    }

    public boolean addPlayer(ServerPlayerEntity player) {
        if (!this.players.containsKey(player)) {
            this.players.put(player, PlayerSnapshot.take(player));
            this.notifyAddPlayer(player);

            return true;
        }

        return false;
    }

    public void removePlayer(ServerPlayerEntity player) {
        PlayerSnapshot snapshot = this.players.remove(player);
        if (snapshot != null) {
            this.notifyRemovePlayer(player);
            snapshot.restore(player);
        }
    }

    private void notifyAddPlayer(ServerPlayerEntity player) {
        this.invoker(PlayerAddListener.EVENT).onAddPlayer(player);
    }

    private void notifyRemovePlayer(ServerPlayerEntity player) {
        this.invoker(PlayerRemoveListener.EVENT).onRemovePlayer(player);
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public Set<ServerPlayerEntity> getPlayers() {
        return this.players.keySet();
    }

    public boolean containsPlayer(ServerPlayerEntity player) {
        return this.players.containsKey(player);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> T invoker(EventType<T> event) {
        return (T) this.invokers.computeIfAbsent(event, EventType::createEmpty);
    }

    public RuleResult testRule(GameRule rule) {
        return this.rules.test(rule);
    }

    public void tick() {
        if (this.closed) {
            return;
        }

        this.invoker(GameTickListener.EVENT).onTick();
    }

    public StartResult requestStart() {
        return this.invoker(RequestStartListener.EVENT).requestStart();
    }

    public JoinResult offerPlayer(ServerPlayerEntity player) {
        JoinResult result = this.invoker(OfferPlayerListener.EVENT).offerPlayer(player);
        if (result.isErr()) {
            return result;
        }

        if (this.addPlayer(player)) {
            return JoinResult.ok();
        } else {
            return JoinResult.alreadyJoined();
        }
    }

    public void closeWorld() {
        if (this.closed) {
            return;
        }

        this.closed = true;
        this.closeGame();

        Reference2ObjectMaps.fastForEach(this.players, entry -> {
            ServerPlayerEntity player = entry.getKey();
            PlayerSnapshot snapshot = entry.getValue();
            snapshot.restore(player);
        });

        this.state.closeWorld();
    }
}
