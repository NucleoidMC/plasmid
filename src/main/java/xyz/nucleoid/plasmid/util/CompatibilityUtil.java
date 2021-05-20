package xyz.nucleoid.plasmid.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import xyz.nucleoid.disguiselib.casts.EntityDisguise;

public class CompatibilityUtil {
    public static final boolean DISGUISELIB_COMPATIBILITY = FabricLoader.getInstance().isModLoaded("disguiselib");

    public static double getClientEntityHeight(Entity entity) {
        if (DISGUISELIB_COMPATIBILITY) {
            return getDisguisedHeight(entity)
        }

        return entity.getHeight();
    }

    private static double getDisguisedHeight(Entity entity) {
        if (entity instanceof EntityDisguise && ((EntityDisguise) entity).isDisguised()) {
            return ((EntityDisguise) entity).getDisguiseEntity().getHeight();
        }

        return entity.getHeight();

    }
}
