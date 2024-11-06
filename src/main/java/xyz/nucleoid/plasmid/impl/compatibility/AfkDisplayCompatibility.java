package xyz.nucleoid.plasmid.impl.compatibility;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import io.github.beabfc.afkdisplay.AfkPlayer;

public final class AfkDisplayCompatibility {
    private static final boolean ENABLED = FabricLoader.getInstance().isModLoaded("afkdisplay");

    public static boolean isActive(ServerPlayerEntity player) {
        if (ENABLED) {
            return !isAfk(player);
        } else {
            return true;
        }
    }

    private static boolean isAfk(ServerPlayerEntity player) {
        var afkPlayer = (AfkPlayer) player;
        return afkPlayer.isAfk();
    }
}
