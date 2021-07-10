package xyz.nucleoid.plasmid.map.workspace;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.world.generator.VoidChunkGenerator;
import xyz.nucleoid.plasmid.map.workspace.editor.WorkspaceEditor;
import xyz.nucleoid.plasmid.map.workspace.editor.WorkspaceEditorManager;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class MapWorkspaceManager extends PersistentState {
    public static final String KEY = Plasmid.ID + ":map_workspaces";

    private static final BlockBounds DEFAULT_BOUNDS = new BlockBounds(-16, 64, -16, 16, 96, 16);

    private final MinecraftServer server;

    private final Map<Identifier, MapWorkspace> workspacesById = new Object2ObjectOpenHashMap<>();
    private final Map<RegistryKey<World>, MapWorkspace> workspacesByDimension = new Reference2ObjectOpenHashMap<>();

    private final WorkspaceEditorManager editorManager;

    private MapWorkspaceManager(MinecraftServer server) {
        this.server = server;

        this.editorManager = new WorkspaceEditorManager();
    }

    public static MapWorkspaceManager get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(
                nbt -> MapWorkspaceManager.readNbt(server, nbt),
                () -> new MapWorkspaceManager(server),
                KEY
        );
    }

    public void tick() {
        this.editorManager.tick();
    }

    @Nullable
    public WorkspaceEditor getEditorFor(ServerPlayerEntity player) {
        return this.editorManager.getEditorFor(player);
    }

    public void onPlayerAddToWorld(ServerPlayerEntity player, ServerWorld world) {
        this.editorManager.onPlayerAddToWorld(player, world);
    }

    public void onPlayerRemoveFromWorld(ServerPlayerEntity player, ServerWorld world) {
        this.editorManager.onPlayerRemoveFromWorld(player, world);
    }

    public MapWorkspace open(Identifier identifier) {
        return this.open(identifier, this.createDefaultConfig());
    }

    public MapWorkspace open(Identifier identifier, RuntimeWorldConfig config) {
        MapWorkspace existingWorkspace = this.workspacesById.get(identifier);
        if (existingWorkspace != null) {
            return existingWorkspace;
        }

        RuntimeWorldHandle worldHandle = this.getOrCreateDimension(identifier, config);
        worldHandle.setTickWhenEmpty(false);

        MapWorkspace workspace = new MapWorkspace(worldHandle, identifier, DEFAULT_BOUNDS);
        this.workspacesById.put(identifier, workspace);
        this.workspacesByDimension.put(worldHandle.asWorld().getRegistryKey(), workspace);
        this.editorManager.addWorkspace(workspace);

        return workspace;
    }

    public boolean delete(MapWorkspace workspace) {
        if (this.workspacesById.remove(workspace.getIdentifier(), workspace)) {
            ServerWorld world = workspace.getWorld();
            this.workspacesByDimension.remove(world.getRegistryKey());

            for (ServerPlayerEntity player : new ArrayList<>(world.getPlayers())) {
                ReturnPosition returnPosition = WorkspaceTraveler.getLeaveReturn(player);
                if (returnPosition != null) {
                    returnPosition.applyTo(player);
                }
            }

            this.editorManager.removeWorkspace(workspace);

            workspace.getWorldHandle().delete();

            return true;
        }

        return false;
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

    private static MapWorkspaceManager readNbt(MinecraftServer server, NbtCompound nbt) {
        MapWorkspaceManager manager = new MapWorkspaceManager(server);

        for (String key : nbt.getKeys()) {
            Identifier identifier = new Identifier(key);
            NbtCompound root = nbt.getCompound(key);

            RuntimeWorldHandle worldHandle = manager.getOrCreateDimension(identifier, manager.createDefaultConfig());
            worldHandle.setTickWhenEmpty(false);

            MapWorkspace workspace = MapWorkspace.deserialize(worldHandle, root);
            manager.workspacesById.put(identifier, workspace);
            manager.workspacesByDimension.put(worldHandle.asWorld().getRegistryKey(), workspace);
            manager.editorManager.addWorkspace(workspace);
        }

        return manager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (Map.Entry<Identifier, MapWorkspace> entry : this.workspacesById.entrySet()) {
            String key = entry.getKey().toString();
            nbt.put(key, entry.getValue().serialize(new NbtCompound()));
        }
        return nbt;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    private RuntimeWorldHandle getOrCreateDimension(Identifier identifier, RuntimeWorldConfig config) {
        Identifier dimensionId = new Identifier(identifier.getNamespace(), "workspace_" + identifier.getPath());
        return Fantasy.get(this.server).getOrOpenPersistentWorld(dimensionId, config);
    }

    private RuntimeWorldConfig createDefaultConfig() {
        DynamicRegistryManager registries = this.server.getRegistryManager();
        VoidChunkGenerator generator = new VoidChunkGenerator(registries.get(Registry.BIOME_KEY));

        return new RuntimeWorldConfig()
                .setDimensionType(DimensionType.OVERWORLD_REGISTRY_KEY)
                .setGenerator(generator);
    }
}
