package xyz.nucleoid.plasmid.game.map.template;

import org.jetbrains.annotations.Nullable;

public interface MapTemplateViewer {
    void setViewing(StagingMapTemplate map);

    @Nullable
    StagingMapTemplate getViewing();
}
