package xyz.nucleoid.plasmid.entity;

import org.jetbrains.annotations.Nullable;

public interface CustomizableEntity {
    void setCustomEntity(CustomEntity customEntity);

    @Nullable
    CustomEntity getCustomEntity();
}
