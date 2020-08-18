package xyz.nucleoid.plasmid.world.bubble;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.game.player.PlayerSnapshot;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class BubbleWorld implements AutoCloseable {
    private final ServerWorld world;
    private final BubbleWorldConfig config;

    private final BubbleWorldListeners listeners = new BubbleWorldListeners();

    private final Map<ServerPlayerEntity, PlayerSnapshot> players = new HashMap<>();

    private BubbleWorld(ServerWorld world, BubbleWorldConfig config) {
        this.world = world;
        this.config = config;

        this.open();
    }

    @Nullable
    public static BubbleWorld forWorld(World world) {
        return ((BubbleWorldHolder) world).getBubbleWorld();
    }

    public static CompletableFuture<Optional<BubbleWorld>> tryOpen(MinecraftServer server, BubbleWorldConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            ServerWorld world = findFreeWorld(server);
            if (world == null) {
                return Optional.empty();
            }

            BubbleWorld bubbleWorld = BubbleWorld.tryCreate(world, config);
            if (bubbleWorld == null) {
                return Optional.empty();
            }

            ((BubbleWorldHolder) world).setBubbleWorld(bubbleWorld);

            return Optional.of(bubbleWorld);
        }, server);
    }

    @Nullable
    private static ServerWorld findFreeWorld(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            if (BubbleWorld.forWorld(world) == null && BubbleWorld.accepts(world)) {
                return world;
            }
        }
        return null;
    }

    @Nullable
    private static BubbleWorld tryCreate(ServerWorld world, BubbleWorldConfig config) {
        if (!BubbleWorld.accepts(world)) {
            return null;
        }

        return new BubbleWorld(world, config);
    }

    private static boolean accepts(ServerWorld world) {
        return world.getChunkManager().getChunkGenerator() instanceof BubbleChunkGenerator;
    }

    private void open() {
        this.assertServerThread();
        this.kickPlayers();

        ServerChunkManager chunkManager = this.world.getChunkManager();

        ChunkGenerator generator = this.config.getGenerator();

        BubbleChunkGenerator bubbleGenerator = (BubbleChunkGenerator) chunkManager.getChunkGenerator();
        if (generator != null) {
            bubbleGenerator.setGenerator(generator);
        } else {
            bubbleGenerator.clearGenerator();
        }

        BubbleWorldControl.enable(this.world);
    }

    @Override
    public void close() {
        this.assertServerThread();

        try {
            this.kickPlayers();

            ServerChunkManager chunkManager = this.world.getChunkManager();

            BubbleChunkGenerator bubbleGenerator = (BubbleChunkGenerator) chunkManager.getChunkGenerator();
            bubbleGenerator.clearGenerator();

            BubbleWorldControl.disable(this.world);
        } finally {
            ((BubbleWorldHolder) this.world).setBubbleWorld(null);
        }
    }

    public boolean addPlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        if (!this.players.containsKey(player)) {
            this.players.put(player, PlayerSnapshot.take(player));
            this.joinPlayer(player);
            return true;
        }

        return false;
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        PlayerSnapshot snapshot = this.players.remove(player);
        if (snapshot != null) {
            this.listeners.removePlayer.accept(player);
            snapshot.restore(player);
            return true;
        }

        return false;
    }

    public void kickPlayer(ServerPlayerEntity player) {
        if (!this.removePlayer(player) || player.world == this.world) {
            ServerWorld overworld = this.world.getServer().getOverworld();

            BlockPos spawnPos = overworld.getSpawnPos();
            player.teleport(overworld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0.0F, 0.0F);
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

    public void kickPlayers() {
        this.assertServerThread();

        List<ServerPlayerEntity> players = new ArrayList<>(this.world.getPlayers());
        for (ServerPlayerEntity player : players) {
            this.kickPlayer(player);
        }
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public RegistryKey<World> getDimensionKey() {
        return this.world.getRegistryKey();
    }

    public BubbleWorldConfig getConfig() {
        return this.config;
    }

    public BubbleWorldListeners getListeners() {
        return this.listeners;
    }

    public Set<ServerPlayerEntity> getPlayers() {
        return this.players.keySet();
    }

    public boolean containsPlayer(ServerPlayerEntity player) {
        return this.players.containsKey(player);
    }

    private void assertServerThread() {
        Thread currentThread = Thread.currentThread();
        if (currentThread != this.world.getServer().getThread()) {
            throw new UnsupportedOperationException("cannot execute on " + currentThread + ": expected server thread!");
        }
    }
}
