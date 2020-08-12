package xyz.nucleoid.plasmid.game;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.GameRuleSet;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorld;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorldConfig;
import xyz.nucleoid.plasmid.util.Scheduler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class GameWorld implements AutoCloseable {
    private static final Map<RegistryKey<World>, GameWorld> DIMENSION_TO_WORLD = new Reference2ObjectOpenHashMap<>();

    private final BubbleWorld bubble;
    private final ServerWorld world;

    private Map<EventType<?>, Object> invokers = new HashMap<>();
    private GameRuleSet rules = GameRuleSet.empty();

    private boolean closed;

    private GameWorld(BubbleWorld bubble) {
        this.bubble = bubble;
        this.world = bubble.getWorld();
    }

    public static GameWorld open(MinecraftServer server, BubbleWorldConfig config) {
        BubbleWorld bubble = BubbleWorld.tryOpen(server, config);
        if (bubble == null) {
            throw new GameOpenException(new LiteralText("No available bubble worlds!"));
        }

        GameWorld gameWorld = new GameWorld(bubble);
        DIMENSION_TO_WORLD.put(bubble.getDimensionKey(), gameWorld);

        return gameWorld;
    }

    @Nullable
    public static GameWorld forWorld(World world) {
        return DIMENSION_TO_WORLD.get(world.getRegistryKey());
    }

    public static Collection<GameWorld> getOpen() {
        return DIMENSION_TO_WORLD.values();
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public void openGame(Consumer<Game> builder) {
        Game game = new Game();
        builder.accept(game);

        this.setGame(game);
    }

    public void setGame(Game game) {
        this.closeGame();

        this.invokers = game.getInvokers();
        this.rules = game.getRules();

        for (ServerPlayerEntity player : this.bubble.getPlayers()) {
            this.invoker(PlayerAddListener.EVENT).onAddPlayer(player);
        }

        this.invoker(GameOpenListener.EVENT).onOpen();
    }

    private void closeGame() {
        this.invoker(GameCloseListener.EVENT).onClose();
        this.invokers = new HashMap<>();
        this.rules = GameRuleSet.empty();
    }

    public boolean addPlayer(ServerPlayerEntity player) {
        if (this.bubble.addPlayer(player)) {
            Scheduler.INSTANCE.submit(server -> {
                this.notifyAddPlayer(player);
            });
            return true;
        }

        return false;
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        if (this.bubble.removePlayer(player)) {
            Scheduler.INSTANCE.submit(server -> {
                this.notifyRemovePlayer(player);
            });
            return true;
        }
        return false;
    }

    private void notifyAddPlayer(ServerPlayerEntity player) {
        this.invoker(PlayerAddListener.EVENT).onAddPlayer(player);
    }

    private void notifyRemovePlayer(ServerPlayerEntity player) {
        this.invoker(PlayerRemoveListener.EVENT).onRemovePlayer(player);
    }

    public int getPlayerCount() {
        return this.bubble.getPlayers().size();
    }

    public Set<ServerPlayerEntity> getPlayers() {
        return this.bubble.getPlayers();
    }

    public boolean containsPlayer(ServerPlayerEntity player) {
        return this.bubble.getPlayers().contains(player);
    }

    /**
     * Returns whether this {@link GameWorld} contains the given {@link LivingEntity}.
     *
     * @param entity  {@link LivingEntity} to check existence of
     * @return        whether the given {@link LivingEntity} exists in this {@link GameWorld}
     */
    public boolean containsEntity(LivingEntity entity) {
        return this.bubble.getWorld().getEntity(entity.getUuid()) != null;
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

    @Override
    public void close() {
        if (this.closed) {
            return;
        }

        this.closed = true;

        Scheduler.INSTANCE.submit(server -> {
            this.closeGame();
            for (ServerPlayerEntity player : this.bubble.getPlayers()) {
                this.notifyRemovePlayer(player);
            }

            this.bubble.close();

            DIMENSION_TO_WORLD.remove(this.world.getRegistryKey(), this);
        });
    }
}
