package net.gegy1000.plasmid.entity;

import javax.annotation.Nullable;

public interface CustomizableEntity {
    void setCustomEntity(CustomEntity customEntity);

    @Nullable
    CustomEntity getCustomEntity();
}
