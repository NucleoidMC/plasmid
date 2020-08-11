package xyz.nucleoid.plasmid.game;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import xyz.nucleoid.plasmid.game.world.ChunkLoadControl;
import xyz.nucleoid.plasmid.game.world.ClearChunks;
import xyz.nucleoid.plasmid.game.world.generator.DynamicChunkGenerator;
import xyz.nucleoid.plasmid.game.world.generator.VoidChunkGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import javax.annotation.Nullable;
import java.util.Map;

public final class GameWorldState {
    private static final Map<RegistryKey<World>, GameWorldState> DIMENSION_TO_STATE = new Reference2ObjectOpenHashMap<>();

    final MinecraftServer server;
    final ServerWorld world;
    final DynamicChunkGenerator chunkGenerator;

    private GameWorld openWorld;

    private GameWorldState(MinecraftServer server, ServerWorld world, DynamicChunkGenerator chunkGenerator) {
        this.server = server;
        this.world = world;
        this.chunkGenerator = chunkGenerator;
    }

    @Nullable
    public static GameWorldState forWorld(World world) {
        RegistryKey<World> dimension = world.getRegistryKey();

        GameWorldState worldState = DIMENSION_TO_STATE.get(dimension);
        if (worldState == null) {
            worldState = tryCreate(world.getServer(), dimension);
            DIMENSION_TO_STATE.put(dimension, worldState);
        }

        return worldState;
    }

    private static GameWorldState tryCreate(MinecraftServer server, RegistryKey<World> dimension) {
        ServerWorld world = server.getWorld(dimension);
        if (world == null) {
            return null;
        }

        ServerChunkManager chunkManager = world.getChunkManager();

        ChunkGenerator worldGenerator = chunkManager.getChunkGenerator();
        if (!(worldGenerator instanceof DynamicChunkGenerator)) {
            return null;
        }

        DynamicChunkGenerator dynamicGenerator = (DynamicChunkGenerator) worldGenerator;
        return new GameWorldState(server, world, dynamicGenerator);
    }

    public GameWorld openWorld(ChunkGenerator generator) {
        if (this.openWorld != null) {
            throw new IllegalStateException("world is already open");
        }

        ServerChunkManager chunkManager = this.world.getChunkManager();
        ((ClearChunks) chunkManager).clearChunks();
        ((ChunkLoadControl) chunkManager).enable();

        this.chunkGenerator.setGenerator(generator);
        this.openWorld = new GameWorld(this);

        return this.openWorld;
    }

    void closeWorld() {
        if (this.openWorld == null) {
            throw new IllegalStateException("world is not open");
        }

        this.openWorld = null;
        this.chunkGenerator.setGenerator(VoidChunkGenerator.INSTANCE);

        ServerChunkManager chunkManager = this.world.getChunkManager();
        ((ClearChunks) chunkManager).clearChunks();
        ((ChunkLoadControl) chunkManager).disable();
    }

    @Nullable
    public GameWorld getOpenWorld() {
        return this.openWorld;
    }

    public boolean isOpen() {
        return this.openWorld != null;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public RegistryKey<World> getDimension() {
        return this.world.getRegistryKey();
    }

    public MinecraftServer getServer() {
        return this.server;
    }
}
