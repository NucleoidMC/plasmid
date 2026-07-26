package xyz.nucleoid.plasmid.impl.compatibility;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class DisguiseLibCompatibility {
    private static final boolean ENABLED = false;// FabricLoader.getInstance().isModLoaded("disguiselib");

    public static double getEntityHeight(Entity entity) {
        if (ENABLED) {
            return getDisguisedHeight(entity);
        } else {
            return entity.getBbHeight();
        }
    }

    private static double getDisguisedHeight(Entity entity) {
        var disguise = getDisguiseFor(entity);
        return disguise != null ? disguise.getBbHeight() : entity.getBbHeight();
    }

    @Nullable
    private static Entity getDisguiseFor(Entity entity) {
        //if (entity instanceof EntityDisguise disguised && disguised.isDisguised()) {
        //    return disguised.getDisguiseEntity();
        //} else {
            return null;
        //}
    }
}
