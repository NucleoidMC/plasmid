package xyz.nucleoid.plasmid.map.workspace;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.util.BlockBounds;

public interface WorkspaceListener {
    default void onSetBounds(BlockBounds bounds) {
    }

    default void onSetOrigin(BlockPos origin) {
    }

    default void onAddRegion(WorkspaceRegion region) {
    }

    default void onRemoveRegion(WorkspaceRegion region) {
    }

    default void onUpdateRegion(WorkspaceRegion lastRegion, WorkspaceRegion newRegion) {
    }
}
