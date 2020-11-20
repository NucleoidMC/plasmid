package xyz.nucleoid.plasmid.game.map.template;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.PersistentWorldHandle;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.world.generator.VoidChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MapWorkspaceManager extends PersistentState {
    public static final String KEY = Plasmid.ID + ":map_workspaces";

    private static final BlockBounds DEFAULT_BOUNDS = new BlockBounds(-16, 64, -16, 16, 96, 16);

    private final MinecraftServer server;

    private final Map<Identifier, MapWorkspace> workspacesById = new Object2ObjectOpenHashMap<>();
    private final Map<RegistryKey<World>, MapWorkspace> workspacesByDimension = new Reference2ObjectOpenHashMap<>();

    private MapWorkspaceManager(MinecraftServer server) {
        super(KEY);
        this.server = server;
    }

    public static MapWorkspaceManager get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(() -> new MapWorkspaceManager(server), KEY);
    }

    public CompletableFuture<MapWorkspace> open(Identifier identifier) {
        MapWorkspace existingWorkspace = this.workspacesById.get(identifier);
        if (existingWorkspace != null) {
            return CompletableFuture.completedFuture(existingWorkspace);
        }

        CompletableFuture<PersistentWorldHandle> dimension = this.getOrCreateDimension(identifier);
        return dimension.thenApplyAsync(worldHandle -> {
            MapWorkspace workspace = new MapWorkspace(worldHandle, identifier, DEFAULT_BOUNDS);
            this.workspacesById.put(identifier, workspace);
            this.workspacesByDimension.put(worldHandle.asWorld().getRegistryKey(), workspace);
            return workspace;
        }, this.server);
    }

    @Nullable
    public MapWorkspace byId(Identifier identifier) {
        return this.workspacesById.get(identifier);
    }

    @Nullable
    public MapWorkspace byDimension(RegistryKey<World> dimension) {
        return this.workspacesByDimension.get(dimension);
    }

    public boolean isWorkspace(RegistryKey<World> dimension) {
        return this.workspacesByDimension.containsKey(dimension);
    }

    public Set<Identifier> getWorkspaceIds() {
        return this.workspacesById.keySet();
    }

    public Collection<MapWorkspace> getWorkspaces() {
        return this.workspacesById.values();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.workspacesById.clear();
        this.workspacesByDimension.clear();

        for (String key : tag.getKeys()) {
            Identifier identifier = new Identifier(key);
            CompoundTag root = tag.getCompound(key);

            this.getOrCreateDimension(identifier).thenAcceptAsync(worldHandle -> {
                MapWorkspace workspace = MapWorkspace.deserialize(worldHandle, root);
                this.workspacesById.put(identifier, workspace);
                this.workspacesByDimension.put(worldHandle.asWorld().getRegistryKey(), workspace);
            }, this.server);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        for (Map.Entry<Identifier, MapWorkspace> entry : this.workspacesById.entrySet()) {
            String key = entry.getKey().toString();
            tag.put(key, entry.getValue().serialize(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    private CompletableFuture<PersistentWorldHandle> getOrCreateDimension(Identifier identifier) {
        Identifier dimensionId = new Identifier(identifier.getNamespace(), "workspace_" + identifier.getPath());
        return Fantasy.get(this.server).getOrOpenPersistentWorld(dimensionId, this::createDimensionOptions);
    }

    private DimensionOptions createDimensionOptions() {
        DynamicRegistryManager registries = this.server.getRegistryManager();
        DimensionType overworld = registries.getDimensionTypes().get(DimensionType.OVERWORLD_REGISTRY_KEY);
        VoidChunkGenerator generator = new VoidChunkGenerator(registries.get(Registry.BIOME_KEY));

        return new DimensionOptions(() -> overworld, generator);
    }
}
