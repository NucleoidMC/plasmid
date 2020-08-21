package xyz.nucleoid.plasmid.world.bubble;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSnapshot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class BubbleWorld implements AutoCloseable {
    private final ServerWorld world;
    private final BubbleWorldConfig config;
    private final Identifier bubbleKey;

    private final PlayerSet players = new PlayerSet();
    private final Map<ServerPlayerEntity, PlayerSnapshot> playerSnapshots = new Object2ObjectOpenHashMap<>();

    BubbleWorld(ServerWorld world, BubbleWorldConfig config, Identifier bubbleKey) {
        this.world = world;
        this.config = config;
        this.bubbleKey = bubbleKey;

        this.open();
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

    private void open() {
        this.assertServerThread();
        this.kickPlayers();
    }

    @Override
    public void close() {
        this.assertServerThread();

        this.kickPlayers();

        CloseBubbleWorld.closeBubble(this.world);
        BubbleWorldManager.get(this.world.getServer()).close(this);
    }

    public boolean addPlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        if (!this.players.contains(player)) {
            this.players.add(player);
            this.playerSnapshots.put(player, PlayerSnapshot.take(player));
            this.joinPlayer(player);
            return true;
        }

        return false;
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        this.assertServerThread();

        boolean removed = this.players.remove(player);

        PlayerSnapshot snapshot = this.playerSnapshots.remove(player);
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

    public Set<ServerPlayerEntity> getPlayers() {
        return this.playerSnapshots.keySet();
    }

    // TODO: 0.4: this should entirely replace getPlayers()
    public PlayerSet getPlayerSet() {
        return this.players;
    }

    public boolean containsPlayer(ServerPlayerEntity player) {
        return this.players.contains(player);
    }

    private void assertServerThread() {
        Thread currentThread = Thread.currentThread();
        if (currentThread != this.world.getServer().getThread()) {
            throw new UnsupportedOperationException("cannot execute on " + currentThread + ": expected server thread!");
        }
    }
}
