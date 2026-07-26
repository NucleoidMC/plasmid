package xyz.nucleoid.plasmid.api.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProvider;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.map.template.processor.MapTemplateProcessor;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuEntryConfig;

public class PlasmidRegistryKeys {
    public static final ResourceKey<Registry<GameType<?>>> GAME_TYPE = createKey("game_type");
    public static final ResourceKey<Registry<MapCodec<? extends GamePortalConfig>>> GAME_PORTAL_CONFIG = createKey("game_portal_config");
    public static final ResourceKey<Registry<MapCodec<? extends MenuEntryConfig>>> MENU_ENTRY = createKey("menu_entry");
    public static final ResourceKey<Registry<GameConfig<?>>> GAME_CONFIG = createKey("game");
    public static final ResourceKey<Registry<MapCodec<? extends TeamListProvider>>> TEAM_LIST_PROVIDER_TYPE = createKey("team_list_provider_type");
    public static final ResourceKey<Registry<MapCodec<? extends MapTemplateProcessor>>> MAP_TEMPLATE_PROCESSOR_TYPE = createKey("map_template_processor_type");

    private static <T> ResourceKey<Registry<T>> createKey(String key) {
        return ResourceKey.createRegistryKey(Plasmid.id(key));
    }
}