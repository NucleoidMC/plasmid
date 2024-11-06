package xyz.nucleoid.plasmid.impl.game.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.api.event.GameEvents;
import xyz.nucleoid.stimuli.EventSource;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.StimulusEvent;
import xyz.nucleoid.stimuli.selector.EventListenerSelector;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class GameSpaceManagerImpl implements GameSpaceManager {
    private static final GameSpaceManagerImpl FALLBACK = new GameSpaceManagerImpl(null);
    private static GameSpaceManagerImpl instance;

    @Nullable
    private final MinecraftServer server;

    private final GameSpaceUserIdManager userIds = new GameSpaceUserIdManager();

    private final List<ManagedGameSpace> gameSpaces = new ArrayList<>();

    private final Map<UUID, ManagedGameSpace> idToGameSpace = new Object2ObjectOpenHashMap<>();
    private final Map<Identifier, ManagedGameSpace> userIdToGameSpace = new Object2ObjectOpenHashMap<>();
    private final Map<RegistryKey<World>, ManagedGameSpace> dimensionToGameSpace = new Reference2ObjectOpenHashMap<>();
    private final Map<UUID, ManagedGameSpace> playerToGameSpace = new Object2ObjectOpenHashMap<>();

    private final ListenerSelector listenerSelector = new ListenerSelector();

    private GameSpaceManagerImpl(@Nullable MinecraftServer server) {
        this.server = server;
    }

    public static void openServer(MinecraftServer server) {
        var instance = GameSpaceManagerImpl.instance;
        if (instance != null) {
            instance.close();
        }

        instance = new GameSpaceManagerImpl(server);
        Stimuli.registerSelector(instance.listenerSelector);

        GameSpaceManagerImpl.instance = instance;
    }

    public static void startClosing() {
        var instance = GameSpaceManagerImpl.instance;
        if (instance != null) {
            instance.close();
        }
    }

    public static void closeServer() {
        GameSpaceManagerImpl.instance = null;
    }

    public static GameSpaceManagerImpl get() {
        if (instance != null) {
            return instance;
        } else {
            return FALLBACK;
        }
    }

    @Override
    public CompletableFuture<GameSpace> open(RegistryEntry<GameConfig<?>> config) {
        if (this.server == null) {
            return CompletableFuture.failedFuture(new RuntimeException("Not initialized yet!"));
        }
        return CompletableFuture.supplyAsync(
                () -> config.value().openProcedure(this.server),
                Util.getMainWorkerExecutor()
        ).thenApplyAsync(
                procedure -> this.addGameSpace(procedure.configOverride() != null ? procedure.configOverride() : config, config, procedure),
                this.server
        );
    }

    private ManagedGameSpace addGameSpace(RegistryEntry<GameConfig<?>> config, RegistryEntry<GameConfig<?>> sourceConfig, GameOpenProcedure procedure) {
        if (this.server == null) {
            throw new RuntimeException("Not initialized yet!");
        }
        var id = UUID.randomUUID();

        var userId = this.userIds.acquire(config.value());
        Preconditions.checkState(!this.userIdToGameSpace.containsKey(userId), "duplicate GameSpace user id acquired");

        var metadata = new GameSpaceMetadata(id, userId, config, sourceConfig);

        var gameSpace = new ManagedGameSpace(this.server, this, metadata);

        this.gameSpaces.add(gameSpace);
        this.idToGameSpace.put(id, gameSpace);
        this.userIdToGameSpace.put(userId, gameSpace);

        Plasmid.LOGGER.info("Game space {} (source: {}) opened", id, GameConfig.sourceName(config));
        GameEvents.OPENED.invoker().onGameSpaceOpened(config, gameSpace);

        procedure.apply(gameSpace);

        return gameSpace;
    }

    @Override
    public Collection<ManagedGameSpace> getOpenGameSpaces() {
        return this.gameSpaces;
    }

    @Override
    @Nullable
    public ManagedGameSpace byId(UUID id) {
        if (this.server == null) {
            return null;
        }
        return this.idToGameSpace.get(id);
    }

    @Override
    @Nullable
    public ManagedGameSpace byUserId(Identifier userId) {
        if (this.server == null) {
            return null;
        }
        return this.userIdToGameSpace.get(userId);
    }

    @Override
    @Nullable
    public ManagedGameSpace byWorld(World world) {
        if (this.server == null) {
            return null;
        }
        return this.dimensionToGameSpace.get(world.getRegistryKey());
    }

    @Override
    @Nullable
    public ManagedGameSpace byPlayer(PlayerEntity player) {
        if (this.server == null) {
            return null;
        }
        return this.playerToGameSpace.get(player.getUuid());
    }

    @Override
    public boolean hasGame(World world) {
        if (this.server == null) {
            return false;
        }
        return this.dimensionToGameSpace.containsKey(world.getRegistryKey());
    }

    @Override
    public boolean inGame(PlayerEntity player) {
        if (this.server == null) {
            return false;
        }
        return this.playerToGameSpace.containsKey(player.getUuid());
    }

    void removeGameSpace(ManagedGameSpace gameSpace) {
        var metadata = gameSpace.getMetadata();
        this.idToGameSpace.remove(metadata.id(), gameSpace);
        this.userIdToGameSpace.remove(metadata.userId(), gameSpace);
        this.gameSpaces.remove(gameSpace);
        this.userIds.release(metadata.userId());
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
        this.userIdToGameSpace.clear();
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
                var gameSpace = GameSpaceManagerImpl.this.playerToGameSpace.get(entity.getUuid());
                if (gameSpace != null) {
                    return gameSpace;
                }
            }

            if (source.getDimension() != null) {
                return GameSpaceManagerImpl.this.dimensionToGameSpace.get(source.getDimension());
            }

            return null;
        }
    }
}
