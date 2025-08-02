package xyz.nucleoid.plasmid.api.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuEntryConfig;

public class PlasmidRegistryKeys {
    public static final RegistryKey<Registry<GameType<?>>> GAME_TYPE = register("game");
    public static final RegistryKey<Registry<MapCodec<? extends GamePortalConfig>>> GAME_PORTAL_CONFIG = register("game_portal_config");
    public static final RegistryKey<Registry<MapCodec<? extends MenuEntryConfig>>> MENU_ENTRY = register("menu_entry");
    public static final RegistryKey<Registry<GameConfig<?>>> GAME_CONFIG = register("game");

    private static <T> RegistryKey<Registry<T>> register(String key) {
        return RegistryKey.ofRegistry(Identifier.of(Plasmid.ID, key));
    }
}