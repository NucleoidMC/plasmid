package xyz.nucleoid.plasmid.world.bubble;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSnapshot;
import xyz.nucleoid.plasmid.util.Scheduler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BubbleWorld implements AutoCloseable {
    private final ServerWorld world;
    private final BubbleWorldConfig config;
    private final Identifier bubbleKey;

    private final MutablePlayerSet players;
    private final Map<UUID, PlayerSnapshot> playerSnapshots = new Object2ObjectOpenHashMap<>();

    private final List<PlayerListener> playerListeners = new ArrayList<>();

    BubbleWorld(ServerWorld world, BubbleWorldConfig config, Identifier bubbleKey) {
        this.world = world;
        this.config = config;
        this.bubbleKey = bubbleKey;

        this.players = new MutablePlayerSet(world.getServer());
    }

    @Nullable
    public static BubbleWorld forWorld(World world) {
        return ((BubbleWorldHolder) world).getBubbleWorld();
    }

    public static CompletableFuture<BubbleWorld> open(MinecraftServer server, BubbleWorldConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            BubbleWorldManager manager = BubbleWorldManager.get(server);
            return manager.open(config);
        }, server);
    }

    public void addPlayerListener(PlayerListener listener) {
        this.playerListeners.add(listener);
    }

    @Override
    public void close() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.closeAsync(future);

        future.thenAccept(v -> {
            CloseBubbleWorld.closeBubble(this.world);
            BubbleWorldManager.get(this.world.getServer()).close(this);
        });
    }

    private void closeAsync(CompletableFuture<Void> future) {
        Scheduler.INSTANCE.submit(server -> {
            this.kickPlayers();

            if (this.world.getPlayers().isEmpty() && this.world.getChunkManager().getLoadedChunkCount() <= 0) {
                future.complete(null);
            } else {
                this.closeAsync(future);
            }
        });
    }

    public boolean addPlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        if (this.players.add(player)) {
            this.playerSnapshots.put(player.getUuid(), PlayerSnapshot.take(player));
            for (PlayerListener listener : this.playerListeners) {
                listener.onAddPlayer(player);
            }
            this.joinPlayer(player);
            return true;
        }

        return false;
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        boolean removed = this.players.remove(player);

        if (removed) {
            for (PlayerListener listener : this.playerListeners) {
                listener.onRemovePlayer(player);
            }
        }

        PlayerSnapshot snapshot = this.playerSnapshots.remove(player.getUuid());
        if (snapshot != null) {
            snapshot.restore(player);
        }

        return removed;
    }

    public void kickPlayer(ServerPlayerEntity player) {
        if (!this.removePlayer(player) || player.world == this.world) {
            ServerWorld overworld = this.world.getServer().getOverworld();
            BlockPos spawnPos = overworld.getSpawnPos();
            float spawnAngle = overworld.getSpawnAngle();
            player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
        }
    }

    private void joinPlayer(ServerPlayerEntity player) {
        player.inventory.clear();
        player.getEnderChestInventory().clear();

        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.clearStatusEffects();

        player.setFireTicks(0);
        player.stopFallFlying();

        player.setGameMode(this.config.getDefaultGameMode());

        this.config.getSpawner().spawnPlayer(this.world, player);
    }

    public List<ServerPlayerEntity> kickPlayers() {
        this.assertServerThread();

        List<ServerPlayerEntity> players = new ArrayList<>(this.world.getPlayers());
        for (ServerPlayerEntity player : players) {
            this.kickPlayer(player);
        }

        return players;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public Identifier getBubbleKey() {
        return this.bubbleKey;
    }

    public RegistryKey<World> getDimensionKey() {
        return this.world.getRegistryKey();
    }

    public BubbleWorldConfig getConfig() {
        return this.config;
    }

    public PlayerSet getPlayers() {
        return this.players;
    }

    private void assertServerThread() {
        Thread currentThread = Thread.currentThread();
        Thread serverThread = this.world.getServer().getThread();
        if (currentThread != serverThread) {
            throw new UnsupportedOperationException("cannot execute on " + currentThread.getName() + ": expected server thread (" + serverThread.getName() + ")!");
        }
    }

    public interface PlayerListener {
        default void onAddPlayer(ServerPlayerEntity player) {
        }

        default void onRemovePlayer(ServerPlayerEntity player) {
        }
    }
}
