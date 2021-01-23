package xyz.nucleoid.plasmid.map.workspace.editor;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspace;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceListener;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Map;
import java.util.UUID;

public final class WorkspaceEditorManager {
    private final Map<RegistryKey<World>, WorkspaceHandler> workspaces = new Reference2ObjectOpenHashMap<>();

    public void onPlayerAddToWorld(ServerPlayerEntity player, ServerWorld world) {
        WorkspaceHandler workspace = this.workspaces.get(world.getRegistryKey());
        if (workspace != null) {
            workspace.addEditor(player, this.createeditorFor(player, workspace.workspace));
        }
    }

    public void onPlayerRemoveFromWorld(ServerPlayerEntity player, ServerWorld world) {
        WorkspaceHandler workspace = this.workspaces.get(world.getRegistryKey());
        if (workspace != null) {
            workspace.editors.remove(player.getUuid());
        }
    }

    public void tick() {
        for (WorkspaceHandler workspace : this.workspaces.values()) {
            workspace.tick();
        }
    }

    public void addWorkspace(MapWorkspace workspace) {
        WorkspaceHandler handler = new WorkspaceHandler(workspace);
        workspace.addListener(handler);

        this.workspaces.put(workspace.getWorld().getRegistryKey(), handler);
    }

    public void removeWorkspace(MapWorkspace workspace) {
        this.workspaces.remove(workspace.getWorld().getRegistryKey());
    }

    private WorkspaceEditor createeditorFor(ServerPlayerEntity player, MapWorkspace workspace) {
        return new ServersideWorkspaceEditor(player, workspace);
    }

    @Nullable
    public WorkspaceEditor getEditorFor(ServerPlayerEntity player) {
        WorkspaceHandler workspace = this.workspaces.get(player.getServerWorld().getRegistryKey());
        if (workspace != null) {
            return workspace.editors.get(player.getUuid());
        } else {
            return null;
        }
    }

    private static class WorkspaceHandler implements WorkspaceListener {
        final MapWorkspace workspace;
        final Map<UUID, WorkspaceEditor> editors = new Object2ObjectOpenHashMap<>();

        WorkspaceHandler(MapWorkspace workspace) {
            this.workspace = workspace;
        }

        void addEditor(ServerPlayerEntity player, WorkspaceEditor editor) {
            this.editors.put(player.getUuid(), editor);

            editor.setOrigin(this.workspace.getOrigin());
            editor.setBounds(this.workspace.getBounds());

            for (WorkspaceRegion region : this.workspace.getRegions()) {
                editor.addRegion(region);
            }
        }

        void tick() {
            for (WorkspaceEditor editor : this.editors.values()) {
                editor.tick();
            }
        }

        @Override
        public void onSetBounds(BlockBounds bounds) {
            for (WorkspaceEditor editor : this.editors.values()) {
                editor.setBounds(bounds);
            }
        }

        @Override
        public void onSetOrigin(BlockPos origin) {
            for (WorkspaceEditor editor : this.editors.values()) {
                editor.setOrigin(origin);
            }
        }

        @Override
        public void onAddRegion(WorkspaceRegion region) {
            for (WorkspaceEditor editor : this.editors.values()) {
                editor.addRegion(region);
            }
        }

        @Override
        public void onRemoveRegion(WorkspaceRegion region) {
            for (WorkspaceEditor editor : this.editors.values()) {
                editor.removeRegion(region);
            }
        }

        @Override
        public void onUpdateRegion(WorkspaceRegion lastRegion, WorkspaceRegion newRegion) {
            for (WorkspaceEditor editor : this.editors.values()) {
                editor.updateRegion(lastRegion, newRegion);
            }
        }
    }
}
