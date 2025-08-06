package xyz.nucleoid.plasmid.api.portal.menu;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.portal.menu.*;

public class MenuEntryConfigs {
    public static MapCodec<? extends MenuEntryConfig> GAME = register("game", GameMenuEntryConfig.CODEC);
    public static MapCodec<? extends MenuEntryConfig> PORTAL = register("portal", PortalEntryConfig.CODEC);

    public static MapCodec<? extends MenuEntryConfig> register(Identifier key, MapCodec<? extends MenuEntryConfig> codec) {
        return Registry.register(PlasmidRegistries.MENU_ENTRY, key, codec);
    }

    private static MapCodec<? extends MenuEntryConfig> register(String key, MapCodec<? extends MenuEntryConfig> codec) {
        return register(Plasmid.id(key), codec);
    }
}
