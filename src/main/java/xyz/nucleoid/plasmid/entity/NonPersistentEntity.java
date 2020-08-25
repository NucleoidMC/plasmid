package xyz.nucleoid.plasmid.entity;

import net.minecraft.entity.Entity;

public interface NonPersistentEntity {
    static void setNonPersistent(Entity entity) {
        if (entity instanceof NonPersistentEntity) {
            ((NonPersistentEntity) entity).setNonPersistent();
        }
    }

    void setNonPersistent();
}
