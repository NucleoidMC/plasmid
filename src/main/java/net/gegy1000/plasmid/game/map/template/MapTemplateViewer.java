package net.gegy1000.plasmid.game.map.template;

import javax.annotation.Nullable;

public interface MapTemplateViewer {
    void setViewing(StagingMapTemplate map);

    @Nullable
    StagingMapTemplate getViewing();
}
