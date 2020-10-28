package xyz.nucleoid.plasmid.game.map.template;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a staging map template viewer.
 * <p>
 * A map viewer will see the bounds of the staging map and the regions bounds
 */
public interface MapTemplateViewer {
    void setViewing(StagingMapTemplate map);

    @Nullable
    StagingMapTemplate getViewing();
}
