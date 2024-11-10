package xyz.nucleoid.plasmid.impl.compatibility;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

public final class DisguiseLibCompatibility {
    private static final boolean ENABLED = FabricLoader.getInstance().isModLoaded("disguiselib");

    public static double getEntityHeight(Entity entity) {
        if (ENABLED) {
            return getDisguisedHeight(entity);
        } else {
            return entity.getHeight();
        }
    }

    private static double getDisguisedHeight(Entity entity) {
        var disguise = getDisguiseFor(entity);
        return disguise != null ? disguise.getHeight() : entity.getHeight();
    }

    @Nullable
    private static Entity getDisguiseFor(Entity entity) {
        if (entity instanceof EntityDisguise disguised && disguised.isDisguised()) {
            return disguised.getDisguiseEntity();
        } else {
            return null;
        }
    }
}
