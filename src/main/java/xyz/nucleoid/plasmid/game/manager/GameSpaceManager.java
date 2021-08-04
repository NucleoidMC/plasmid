package xyz.nucleoid.plasmid.game.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.error.ErrorReporter;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.stimuli.EventSource;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.StimulusEvent;
import xyz.nucleoid.stimuli.selector.EventListenerSelector;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class GameSpaceManager {
    private static GameSpaceManager instance;

    private final MinecraftServer server;

    private final GameSpaceIdManager ids = new GameSpaceIdManager();

    private final List<ManagedGameSpace> gameSpaces = new ArrayList<>();

    private final Map<Identifier, ManagedGameSpace> idToGameSpace = new Object2ObjectOpenHashMap<>();
    private final Map<RegistryKey<World>, ManagedGameSpace> dimensionToGameSpace = new Reference2ObjectOpenHashMap<>();
    private final Map<UUID, ManagedGameSpace> playerToGameSpace = new Object2ObjectOpenHashMap<>();

    private final ListenerSelector listenerSelector = new ListenerSelector();

    private GameSpaceManager(MinecraftServer server) {
        this.server = server;
    }

    public static void openServer(MinecraftServer server) {
        var instance = GameSpaceManager.instance;
        if (instance != null) {
            instance.close();
        }

        instance = new GameSpaceManager(server);
        Stimuli.registerSelector(instance.listenerSelector);

        GameSpaceManager.instance = instance;
    }

    public static void startClosing() {
        var instance = GameSpaceManager.instance;
        if (instance != null) {
            instance.close();
        }
    }

    public static void closeServer() {
        GameSpaceManager.instance = null;
    }

    public static GameSpaceManager get() {
        return Preconditions.checkNotNull(instance, "GameSpaceManager not yet initialized");
    }

    public CompletableFuture<ManagedGameSpace> open(GameConfig<?> config) {
        var future = CompletableFuture.supplyAsync(
                () -> config.openProcedure(this.server),
                Util.getMainWorkerExecutor()
        ).thenApplyAsync(
                procedure -> this.addGameSpace(config, procedure),
                this.server
        );

        future.exceptionally(throwable -> {
            if (GameOpenException.unwrap(throwable) == null) {
                try (var reporter = ErrorReporter.open(config)) {
                    reporter.report(throwable, "Opening game");
                }
            }
            return null;
        });

        return future;
    }

    private ManagedGameSpace addGameSpace(GameConfig<?> config, GameOpenProcedure procedure) {
        var id = this.ids.acquire(config);
        Preconditions.checkState(!this.idToGameSpace.containsKey(id), "duplicate GameSpace id acquired");

        var gameSpace = new ManagedGameSpace(this.server, this, config, id);
        procedure.apply(gameSpace);

        this.gameSpaces.add(gameSpace);
        this.idToGameSpace.put(id, gameSpace);

        GameEvents.OPENED.invoker().onGameSpaceOpened(config, gameSpace);

        return gameSpace;
    }

    public Collection<ManagedGameSpace> getOpenGameSpaces() {
        return this.gameSpaces;
    }

    @Nullable
    public ManagedGameSpace byId(Identifier id) {
        return this.idToGameSpace.get(id);
    }

    @Nullable
    public ManagedGameSpace byWorld(World world) {
        return this.dimensionToGameSpace.get(world.getRegistryKey());
    }

    @Nullable
    public ManagedGameSpace byPlayer(PlayerEntity player) {
        return this.playerToGameSpace.get(player.getUuid());
    }

    public boolean hasGame(World world) {
        return this.dimensionToGameSpace.containsKey(world.getRegistryKey());
    }

    public boolean inGame(PlayerEntity player) {
        return this.playerToGameSpace.containsKey(player.getUuid());
    }

    void removeGameSpace(ManagedGameSpace gameSpace) {
        this.idToGameSpace.remove(gameSpace.getId(), gameSpace);
        this.gameSpaces.remove(gameSpace);
        this.ids.release(gameSpace.getId());
    }

    void addDimensionToGameSpace(ManagedGameSpace gameSpace, RegistryKey<World> dimension) {
        this.dimensionToGameSpace.put(dimension, gameSpace);
    }

    void removeDimensionFromGameSpace(ManagedGameSpace gameSpace, RegistryKey<World> dimension) {
        this.dimensionToGameSpace.remove(dimension, gameSpace);
    }

    void addPlayerToGameSpace(ManagedGameSpace gameSpace, ServerPlayerEntity player) {
        this.playerToGameSpace.put(player.getUuid(), gameSpace);
    }

    void removePlayerFromGameSpace(ManagedGameSpace gameSpace, ServerPlayerEntity player) {
        this.playerToGameSpace.remove(player.getUuid(), gameSpace);
    }

    private void close() {
        Stimuli.unregisterSelector(this.listenerSelector);

        var gameSpaces = Lists.newArrayList(this.gameSpaces);
        this.gameSpaces.clear();

        for (var gameSpace : gameSpaces) {
            gameSpace.close(GameCloseReason.CANCELED);
        }

        this.idToGameSpace.clear();
        this.dimensionToGameSpace.clear();
        this.playerToGameSpace.clear();
    }

    final class ListenerSelector implements EventListenerSelector {
        @Override
        public <T> Iterator<T> selectListeners(MinecraftServer server, StimulusEvent<T> event, EventSource source) {
            var gameSpace = this.getGameSpaceFor(source);
            if (gameSpace != null) {
                return gameSpace.getBehavior().getInvokers(event).iterator();
            }

            return Collections.emptyIterator();
        }

        @Nullable
        private ManagedGameSpace getGameSpaceFor(EventSource source) {
            var entity = source.getEntity();
            if (entity instanceof ServerPlayerEntity) {
                var gameSpace = GameSpaceManager.this.playerToGameSpace.get(entity.getUuid());
                if (gameSpace != null) {
                    return gameSpace;
                }
            }

            if (source.getDimension() != null) {
                return GameSpaceManager.this.dimensionToGameSpace.get(source.getDimension());
            }

            return null;
        }
    }
}
