package xyz.nucleoid.plasmid.entity;

import javax.annotation.Nullable;

/**
 * @deprecated in favour of {@link xyz.nucleoid.plasmid.fake.FakeEntityType}
 */
@Deprecated
public interface CustomizableEntity {
    void setCustomEntity(CustomEntity customEntity);

    @Nullable
    CustomEntity getCustomEntity();
}
