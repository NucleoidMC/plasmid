package xyz.nucleoid.plasmid.api.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProvider;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.map.template.processor.MapTemplateProcessor;
import xyz.nucleoid.plasmid.api.menu.*;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class PlasmidRegistryKeys {
    public static final ResourceKey<Registry<GameType<?>>> GAME_TYPE = createKey("game_type");
    public static final ResourceKey<Registry<MapCodec<? extends GameMenuEntryConfig>>> GAME_MENU_ENTRY_TYPE = createKey("game_menu_entry_type");
    public static final ResourceKey<Registry<GameMenuEntryConfig>> GAME_MENU_ENTRY = createKey("game_menu_entry");
    public static final ResourceKey<Registry<GameMenuConfig>> GAME_MENU = createKey("game_menu");
    public static final ResourceKey<Registry<MapCodec<? extends GameMenuTheme>>> GAME_MENU_THEME_TYPE = createKey("game_menu_theme_type");
    public static final ResourceKey<Registry<GameMenuTheme>> GAME_MENU_THEME = createKey("game_menu_theme");
    public static final ResourceKey<Registry<GameMenuFeature>> GAME_MENU_FEATURE = createKey("game_menu_feature");
    public static final ResourceKey<Registry<GameMenuLayoutType>> GAME_MENU_LAYOUT_TYPE = createKey("game_menu_layout_type");
    public static final ResourceKey<Registry<GameConfig<?>>> GAME_CONFIG = createKey("game");
    public static final ResourceKey<Registry<MapCodec<? extends TeamListProvider>>> TEAM_LIST_PROVIDER_TYPE = createKey("team_list_provider_type");
    public static final ResourceKey<Registry<MapCodec<? extends MapTemplateProcessor>>> MAP_TEMPLATE_PROCESSOR_TYPE = createKey("map_template_processor_type");

    private static <T> ResourceKey<Registry<T>> createKey(String key) {
        return ResourceKey.createRegistryKey(Plasmid.id(key));
    }
}