package xyz.nucleoid.plasmid.game;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.Scheduler;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorld;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Represents a unique world with a {@link Game} attached to it.
 *
 * <p>Each world has a {@link net.minecraft.world.gen.chunk.ChunkGenerator} which is invoked as chunks are requested.
 * It is important to note that not all chunks will be loaded on start, and the game logic must take care to handle this.
 * Players can only be added to this game world through {@link GameWorld#addPlayer} or {@link GameWorld#offerPlayer}.
 */
// TODO: split into interface
public final class GameWorld implements AutoCloseable {
    private static final Map<RegistryKey<World>, GameWorld> DIMENSION_TO_WORLD = new Reference2ObjectOpenHashMap<>();

    private final BubbleWorld bubble;
    private final ConfiguredGame<?> configuredGame;

    private final Game emptyGame = new Game(this);
    private final AtomicReference<Game> game = new AtomicReference<>(this.emptyGame);

    private final GameResources resources = new GameResources();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final GameLifecycle lifecycle = new GameLifecycle();

    private GameWorld(BubbleWorld bubble, ConfiguredGame<?> configuredGame) {
        this.bubble = bubble;
        this.configuredGame = configuredGame;

        bubble.addPlayerListener(new BubbleWorld.PlayerListener() {
            @Override
            public void onRemovePlayer(ServerPlayerEntity player) {
                GameWorld.this.onRemovePlayer(player);
            }
        });
    }

    /**
     * Attempts to open a new {@link GameWorld} using the given {@link BubbleWorldConfig}.
     *
     * <p>If a {@link BubbleWorld} could not be opened, a {@link GameOpenException} is thrown.
     *
     * @param server {@link MinecraftServer} to open a {@link GameWorld} in
     * @param game initial game to open this GameWorld with
     * @param config {@link BubbleWorldConfig} to use to construct a {@link GameWorld}
     * @return a future to a {@link GameWorld} if one was opened
     * @throws GameOpenException if a {@link BubbleWorld} could not be opened
     */
    public static CompletableFuture<GameWorld> open(MinecraftServer server, ConfiguredGame<?> game, BubbleWorldConfig config) {
        return BubbleWorld.open(server, config).thenApply(bubble -> {
            GameWorld gameWorld = new GameWorld(bubble, game);
            DIMENSION_TO_WORLD.put(bubble.getDimensionKey(), gameWorld);

            return gameWorld;
        });
    }

    /**
     * Returns the {@link GameWorld} hosted in the given {@link World}.
     *
     * <p>If a {@link GameWorld} does not exist in the given {@link World}, null is returned.
     *
     * @param world world to check for a {@link GameWorld}
     * @return the {@link GameWorld} hosted in the given {@link World}, or null if none are found
     */
    @Nullable
    public static GameWorld forWorld(World world) {
        return DIMENSION_TO_WORLD.get(world.getRegistryKey());
    }

    /**
     * Returns a {@link Collection} of open {@link GameWorld}s across all dimensions.
     *
     * @return all open {@link GameWorld} instances
     */
    public static Collection<GameWorld> getOpen() {
        return DIMENSION_TO_WORLD.values();
    }

    /**
     * Opens a new {@link Game} in this {@link GameWorld} with properties set by the given {@link Consumer}.
     *
     * <p>On the new game, {@link PlayerAddListener} will be invoked with existing players, followed by
     * {@link GameOpenListener} once all players have been added.
     *
     * <p>Before the new game is open, {@link GameCloseListener} will be invoked on the former game instance
     *
     * @param builder {@link Consumer} that modifies the given {@link Game} instance
     */
    public void openGame(Consumer<Game> builder) {
        if (this.closed.get()) {
            return;
        }

        Game game = new Game(this);
        builder.accept(game);

        Scheduler.INSTANCE.submit(server -> {
            Game closedGame = this.game.getAndSet(game);

            closedGame.getResources().close();
            closedGame.getListeners().invoker(GameCloseListener.EVENT).onClose();

            for (ServerPlayerEntity player : this.bubble.getPlayers()) {
                this.invoker(PlayerAddListener.EVENT).onAddPlayer(player);
            }

            this.invoker(GameOpenListener.EVENT).onOpen();
        });
    }

    /**
     * Adds an {@link AutoCloseable} resource to this {@link GameWorld} which will be closed upon game close
     *
     * @param resource the resource to add
     * @param <T> the type of resource
     * @return the added resource
     */
    public <T extends AutoCloseable> T addResource(T resource) {
        return this.resources.add(resource);
    }

    /**
     * Attempts to add the given {@link ServerPlayerEntity} to this game world if it is not already added.
     *
     * <p>{@link GameWorld#offerPlayer} can be used instead to check with {@link OfferPlayerListener} listeners before adding the player.
     *
     * @param player {@link ServerPlayerEntity} to add to this {@link GameWorld}
     * @return whether the {@link ServerPlayerEntity} was successfully added
     */
    public boolean addPlayer(ServerPlayerEntity player) {
        if (this.closed.get()) {
            return false;
        }

        if (this.bubble.addPlayer(player)) {
            this.invoker(PlayerAddListener.EVENT).onAddPlayer(player);
            this.lifecycle.addPlayer(this, player);
            return true;
        }

        return false;
    }

    /**
     * Attempts to removes the given {@link ServerPlayerEntity} from this {@link GameWorld}.
     * When a player is removed, they will be teleported back to their former location prior to joining
     *
     * <p>Implementation is left to this {@link GameWorld}'s {@link BubbleWorld} in {@link BubbleWorld#removePlayer(ServerPlayerEntity)}.
     *
     * @param player {@link ServerPlayerEntity} to remove from this {@link GameWorld}
     * @return whether the {@link ServerPlayerEntity} was successfully removed
     */
    public boolean removePlayer(ServerPlayerEntity player) {
        if (this.closed.get()) {
            return false;
        }
        return this.bubble.removePlayer(player);
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        this.invoker(PlayerRemoveListener.EVENT).onRemovePlayer(player);
        this.lifecycle.removePlayer(this, player);

        if (this.getPlayerCount() <= 0) {
            this.close();
        }
    }

    /**
     * Requests that this {@link GameWorld} begins. Called through the /game start command.
     *
     * <p>The behavior of the game starting is left any {@link RequestStartListener#EVENT} listener attached to it.
     *
     * @return a {@link StartResult} which describes whether the game was able to start
     */
    public CompletableFuture<StartResult> requestStart() {
        return Scheduler.INSTANCE.submit(server -> {
            return this.invoker(RequestStartListener.EVENT).requestStart();
        });
    }

    /**
     * Attempts to add a {@link ServerPlayerEntity} to this {@link GameWorld} by invoking any {@link OfferPlayerListener#EVENT} listeners attached to it.
     *
     * <p>If the event succeeds, {@link JoinResult#ok()} is returned. If the player could not be added to this game world,
     * a {@link JoinResult} error is returned. If any listeners error, its response is returned and the player is not added to this game world.
     *
     * @param player the {@link ServerPlayerEntity} trying to join this {@link GameWorld}
     * @return {@link JoinResult} describing the results of the given {@link ServerPlayerEntity} trying to join
     */
    public CompletableFuture<JoinResult> offerPlayer(ServerPlayerEntity player) {
        return Scheduler.INSTANCE.submit(server -> {
            if (GameWorld.forWorld(player.world) != null) {
                return JoinResult.inOtherGame();
            }

            JoinResult result = this.invoker(OfferPlayerListener.EVENT).offerPlayer(player);
            if (result.isError()) {
                return result;
            }

            if (this.addPlayer(player)) {
                Text joinMessage = new TranslatableText("text.plasmid.game.join", player.getDisplayName())
                        .formatted(Formatting.YELLOW);

                this.getPlayers().sendMessage(joinMessage);

                return JoinResult.ok();
            } else {
                return JoinResult.alreadyJoined();
            }
        });
    }

    /**
     * Closes this {@link GameWorld}.
     *
     * <p>Upon close, all players in this {@link GameWorld} are removed and restored to their state prior to entering this game world.
     */
    @Override
    public void close() {
        if (!this.closed.compareAndSet(false, true)) {
            return;
        }

        Game game = this.game.getAndSet(this.emptyGame);

        Scheduler.INSTANCE.submit(server -> {
            try {
                List<ServerPlayerEntity> players = this.bubble.kickPlayers();

                this.resources.close();
                game.getListeners().invoker(GameCloseListener.EVENT).onClose();
                this.lifecycle.close(this, players);

                this.bubble.close();
            } finally {
                DIMENSION_TO_WORLD.remove(this.bubble.getWorld().getRegistryKey(), this);
            }
        });
    }

    /**
     * Returns all {@link ServerPlayerEntity}s in this {@link GameWorld}.
     *
     * <p>{@link GameWorld#containsPlayer(ServerPlayerEntity)} can be used to check if a {@link ServerPlayerEntity} is in this {@link GameWorld} instead.
     *
     * @return a {@link PlayerSet} that contains all {@link ServerPlayerEntity}s in this {@link GameWorld}
     */
    public PlayerSet getPlayers() {
        return this.bubble.getPlayers();
    }

    /**
     * @return the number of players in this {@link GameWorld}.
     */
    public int getPlayerCount() {
        return this.bubble.getPlayers().size();
    }

    /**
     * Returns whether this {@link GameWorld} contains the given {@link ServerPlayerEntity}.
     *
     * @param player {@link ServerPlayerEntity} to check existence of
     * @return whether the given {@link ServerPlayerEntity} exists in this {@link GameWorld}
     */
    public boolean containsPlayer(ServerPlayerEntity player) {
        return this.bubble.getPlayers().contains(player);
    }

    /**
     * Returns whether this {@link GameWorld} contains the given {@link Entity}.
     *
     * @param entity {@link Entity} to check existence of
     * @return whether the given {@link Entity} exists in this {@link GameWorld}
     */
    public boolean containsEntity(Entity entity) {
        return this.bubble.getWorld().getEntity(entity.getUuid()) != null;
    }

    @Nonnull
    public <T> T invoker(EventType<T> event) {
        Game game = this.game.get();
        return game.getListeners().invoker(event);
    }

    /**
     * Tests whether the given {@link GameRule} passes in this {@link GameWorld}.
     *
     * <p>As an example, calling this method with {@link GameRule#BLOCK_DROPS} will return a {@link RuleResult} that describes whether blocks can drop.
     *
     * @param rule the {@link GameRule} to test in this {@link GameWorld}
     * @return a {@link RuleResult} that describes whether the {@link GameRule} passes
     */
    public RuleResult testRule(GameRule rule) {
        Game game = this.game.get();
        return game.getRules().test(rule);
    }

    /**
     * Returns the {@link ServerWorld} that this {@link GameWorld} is hosted in.
     *
     * @return the host world of this {@link GameWorld}.
     */
    public ServerWorld getWorld() {
        return this.bubble.getWorld();
    }

    public MinecraftServer getServer() {
        return this.bubble.getWorld().getServer();
    }

    public ConfiguredGame<?> getGameConfg() {
        return this.configuredGame;
    }

    public GameLifecycle getLifecycle() {
        return this.lifecycle;
    }
}
