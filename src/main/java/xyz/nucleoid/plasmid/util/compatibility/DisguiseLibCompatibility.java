package xyz.nucleoid.plasmid.util.compatibility;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.disguiselib.casts.EntityDisguise;

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
        Entity disguise = getDisguiseFor(entity);
        return disguise != null ? disguise.getHeight() : entity.getHeight();
    }

    @Nullable
    private static Entity getDisguiseFor(Entity entity) {
        if (entity instanceof EntityDisguise && ((EntityDisguise) entity).isDisguised()) {
            return ((EntityDisguise) entity).getDisguiseEntity();
        } else {
            return null;
        }
    }
}
