package xyz.nucleoid.plasmid.fake;

import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

@Deprecated
public class FakeEntityType<T extends Entity> extends FabricEntityType<T> {
    private final EntityType<T> proxyType;

    private FakeEntityType(FabricEntityType<T> type, EntityType<T> proxyType) {
        super(
                type.factory, type.getSpawnGroup(),
                type.isSaveable(), type.isSummonable(), type.isFireImmune(),
                type.isSpawnableFarFromPlayer(), type.canSpawnInside,
                type.getDimensions(),
                type.getMaxTrackDistance(), type.getTrackTickInterval(), type.alwaysUpdateVelocity()
        );
        this.proxyType = proxyType;
    }

    public static <T extends Entity> FakeEntityType<T> of(FabricEntityType<T> type, EntityType<T> proxyType) {
        return new FakeEntityType<>(type, proxyType);
    }

    public EntityType<T> asProxy() {
        return this.proxyType;
    }
}
