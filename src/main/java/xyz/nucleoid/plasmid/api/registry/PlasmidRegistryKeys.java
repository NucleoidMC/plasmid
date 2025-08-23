package xyz.nucleoid.plasmid.api.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProvider;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuEntryConfig;

public class PlasmidRegistryKeys {
    public static final RegistryKey<Registry<GameType<?>>> GAME_TYPE = createKey("game_type");
    public static final RegistryKey<Registry<MapCodec<? extends GamePortalConfig>>> GAME_PORTAL_CONFIG = createKey("game_portal_config");
    public static final RegistryKey<Registry<MapCodec<? extends MenuEntryConfig>>> MENU_ENTRY = createKey("menu_entry");
    public static final RegistryKey<Registry<GameConfig<?>>> GAME_CONFIG = createKey("game");
    public static final RegistryKey<Registry<MapCodec<? extends TeamListProvider>>> TEAM_LIST_PROVIDER_TYPE = createKey("team_list_provider_type");

    private static <T> RegistryKey<Registry<T>> createKey(String key) {
        return RegistryKey.ofRegistry(Plasmid.id(key));
    }
}