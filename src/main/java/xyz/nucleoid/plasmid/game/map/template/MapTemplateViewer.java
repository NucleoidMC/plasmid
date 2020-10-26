package xyz.nucleoid.plasmid.game.map.template;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a staging map template viewer.
 */
public interface MapTemplateViewer {
    void setViewing(StagingMapTemplate map);

    @Nullable
    StagingMapTemplate getViewing();
}
