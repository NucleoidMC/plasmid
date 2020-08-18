package xyz.nucleoid.plasmid.entity;

import javax.annotation.Nullable;

@Deprecated
public interface CustomizableEntity {
    void setCustomEntity(CustomEntity customEntity);

    @Nullable
    CustomEntity getCustomEntity();
}
