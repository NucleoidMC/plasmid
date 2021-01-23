package xyz.nucleoid.plasmid.map.workspace.editor;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

public interface WorkspaceEditor {
    default void addRegion(WorkspaceRegion region) {
    }

    default void updateRegion(WorkspaceRegion lastRegion, WorkspaceRegion newRegion) {
    }

    default void removeRegion(WorkspaceRegion region) {
    }

    default void setBounds(BlockBounds bounds) {
    }

    default void setOrigin(BlockPos origin) {
    }

    default boolean useRegionItem() {
        return false;
    }

    @Nullable
    default BlockBounds takeTracedRegion() {
        return null;
    }

    default void tick() {
    }
}
