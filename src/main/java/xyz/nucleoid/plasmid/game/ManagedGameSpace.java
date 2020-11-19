package xyz.nucleoid.plasmid.game;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
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
 * Represents the space within which a game occurs through attached {@link GameLogic}
 *
 * <p>Each world has a {@link net.minecraft.world.gen.chunk.ChunkGenerator} which is invoked as chunks are requested.
 * It is important to note that not all chunks will be loaded on start, and the game logic must take care to handle this.
 * Players can only be added to this game space through {@link ManagedGameSpace#addPlayer} or {@link ManagedGameSpace#offerPlayer}.
 */
public final class ManagedGameSpace implements GameSpace, AutoCloseable {
    private static final Map<RegistryKey<World>, ManagedGameSpace> DIMENSION_TO_WORLD = new Reference2ObjectOpenHashMap<>();

    private final BubbleWorld bubble;
    private final ConfiguredGame<?> configuredGame;

    private final GameLogic emptyLogic = new GameLogic(this);
    private final AtomicReference<GameLogic> logic = new AtomicReference<>(this.emptyLogic);

    private final GameResources resources = new GameResources();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final GameLifecycle lifecycle = new GameLifecycle();

    private ManagedGameSpace(BubbleWorld bubble, ConfiguredGame<?> configuredGame) {
        this.bubble = bubble;
        this.configuredGame = configuredGame;

        bubble.addPlayerListener(new BubbleWorld.PlayerListener() {
            @Override
            public void onRemovePlayer(ServerPlayerEntity player) {
                ManagedGameSpace.this.onRemovePlayer(player);
            }
        });
    }

    /**
     * Attempts to open a new {@link GameSpace} using the given {@link BubbleWorldConfig}.
     *
     * <p>If a {@link BubbleWorld} could not be opened, a {@link GameOpenException} is thrown.
     *
     * @param server {@link MinecraftServer} to open a {@link GameSpace} in
     * @param game initial game to open this {@link GameSpace} with
     * @param config {@link BubbleWorldConfig} to use to construct a {@link GameSpace}
     * @return a future to a {@link GameSpace} if one was opened
     * @throws GameOpenException if a {@link BubbleWorld} could not be opened
     */
    public static CompletableFuture<ManagedGameSpace> open(MinecraftServer server, ConfiguredGame<?> game, BubbleWorldConfig config) {
        return BubbleWorld.open(server, config).thenApply(bubble -> {
            ManagedGameSpace gameSpace = new ManagedGameSpace(bubble, game);
            DIMENSION_TO_WORLD.put(bubble.getDimensionKey(), gameSpace);

            return gameSpace;
        });
    }

    /**
     * Returns the {@link ManagedGameSpace} hosted in the given {@link World}.
     *
     * <p>If a {@link ManagedGameSpace} does not exist in the given {@link World}, null is returned.
     *
     * @param world world to check for a {@link ManagedGameSpace}
     * @return the {@link ManagedGameSpace} hosted in the given {@link World}, or null if none are found
     */
    @Nullable
    public static ManagedGameSpace forWorld(World world) {
        return DIMENSION_TO_WORLD.get(world.getRegistryKey());
    }

    /**
     * Returns a {@link Collection} of open {@link ManagedGameSpace}s across all dimensions.
     *
     * @return all open {@link ManagedGameSpace} instances
     */
    public static Collection<ManagedGameSpace> getOpen() {
        return DIMENSION_TO_WORLD.values();
    }

    @Override
    public void openGame(Consumer<GameLogic> builder) {
        if (this.closed.get()) {
            return;
        }

        GameLogic logic = new GameLogic(this);
        builder.accept(logic);

        Scheduler.INSTANCE.submit(server -> {
            GameLogic closedGameLogic = this.logic.getAndSet(logic);

            closedGameLogic.getResources().close();
            closedGameLogic.getListeners().invoker(GameCloseListener.EVENT).onClose();

            for (ServerPlayerEntity player : this.bubble.getPlayers()) {
                this.invoker(PlayerAddListener.EVENT).onAddPlayer(player);
            }

            this.invoker(GameOpenListener.EVENT).onOpen();
        });
    }

    @Override
    public <T extends AutoCloseable> T addResource(T resource) {
        return this.resources.add(resource);
    }

    /**
     * Attempts to add the given {@link ServerPlayerEntity} to this game space if it is not already added.
     *
     * <p>{@link ManagedGameSpace#offerPlayer} can be used instead to check with {@link OfferPlayerListener} listeners before adding the player.
     *
     * @param player {@link ServerPlayerEntity} to add to this {@link ManagedGameSpace}
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
     * Attempts to removes the given {@link ServerPlayerEntity} from this {@link ManagedGameSpace}.
     * When a player is removed, they will be teleported back to their former location prior to joining
     *
     * <p>Implementation is left to this {@link ManagedGameSpace}'s {@link BubbleWorld} in {@link BubbleWorld#removePlayer(ServerPlayerEntity)}.
     *
     * @param player {@link ServerPlayerEntity} to remove from this {@link ManagedGameSpace}
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

    @Override
    public CompletableFuture<StartResult> requestStart() {
        return Scheduler.INSTANCE.submit(server -> {
            return this.invoker(RequestStartListener.EVENT).requestStart();
        });
    }

    /**
     * Attempts to add a {@link ServerPlayerEntity} to this {@link ManagedGameSpace} by invoking any {@link OfferPlayerListener#EVENT} listeners attached to it.
     *
     * <p>If the event succeeds, {@link JoinResult#ok()} is returned. If the player could not be added to this game space,
     * a {@link JoinResult} error is returned. If any listeners error, its response is returned and the player is not added to this game space.
     *
     * @param player the {@link ServerPlayerEntity} trying to join this {@link ManagedGameSpace}
     * @return {@link JoinResult} describing the results of the given {@link ServerPlayerEntity} trying to join
     */
    public CompletableFuture<JoinResult> offerPlayer(ServerPlayerEntity player) {
        return Scheduler.INSTANCE.submit(server -> {
            if (ManagedGameSpace.forWorld(player.world) != null) {
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
     * Closes this {@link GameSpace}.
     *
     * <p>Upon close, all players in this {@link GameSpace} are removed and restored to their state prior to entering this game space.
     */
    @Override
    public void close() {
        if (!this.closed.compareAndSet(false, true)) {
            return;
        }

        GameLogic logic = this.logic.getAndSet(this.emptyLogic);

        Scheduler.INSTANCE.submit(server -> {
            try {
                List<ServerPlayerEntity> players = this.bubble.kickPlayers();

                this.resources.close();
                logic.getListeners().invoker(GameCloseListener.EVENT).onClose();
                this.lifecycle.close(this, players);

                this.bubble.close();
            } finally {
                DIMENSION_TO_WORLD.remove(this.bubble.getWorld().getRegistryKey(), this);
            }
        });
    }

    @Override
    public PlayerSet getPlayers() {
        return this.bubble.getPlayers();
    }

    @Nonnull
    public <T> T invoker(EventType<T> event) {
        GameLogic logic = this.logic.get();
        return logic.getListeners().invoker(event);
    }

    /**
     * Tests whether the given {@link GameRule} passes in this {@link GameSpace}.
     *
     * <p>As an example, calling this method with {@link GameRule#BLOCK_DROPS} will return a {@link RuleResult} that describes whether blocks can drop.
     *
     * @param rule the {@link GameRule} to test in this {@link GameSpace}
     * @return a {@link RuleResult} that describes whether the {@link GameRule} passes
     */
    public RuleResult testRule(GameRule rule) {
        GameLogic logic = this.logic.get();
        return logic.getRules().test(rule);
    }

    @Override
    public ServerWorld getWorld() {
        return this.bubble.getWorld();
    }

    @Override
    public ConfiguredGame<?> getGameConfig() {
        return this.configuredGame;
    }

    @Override
    public GameLifecycle getLifecycle() {
        return this.lifecycle;
    }
}
