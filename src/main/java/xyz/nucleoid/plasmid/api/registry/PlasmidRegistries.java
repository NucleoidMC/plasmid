package xyz.nucleoid.plasmid.api.registry;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProvider;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.map.template.processor.MapTemplateProcessor;
import xyz.nucleoid.plasmid.api.menu.*;

public class PlasmidRegistries {
    public static final Registry<GameType<?>> GAME_TYPE = register(PlasmidRegistryKeys.GAME_TYPE);
    public static final Registry<MapCodec<? extends GameMenuEntryConfig>> GAME_MENU_ENTRY_TYPE = register(PlasmidRegistryKeys.GAME_MENU_ENTRY_TYPE);
    public static final Registry<MapCodec<? extends GameMenuTheme>> GAME_MENU_THEME_TYPE = register(PlasmidRegistryKeys.GAME_MENU_THEME_TYPE);
    public static final Registry<GameMenuFeature> GAME_MENU_FEATURE = register(PlasmidRegistryKeys.GAME_MENU_FEATURE);
    public static final Registry<GameMenuLayoutType> GAME_MENU_LAYOUT_TYPE = register(PlasmidRegistryKeys.GAME_MENU_LAYOUT_TYPE);
    public static final Registry<MapCodec<? extends TeamListProvider>> TEAM_LIST_PROVIDER_TYPE = register(PlasmidRegistryKeys.TEAM_LIST_PROVIDER_TYPE);
    public static final Registry<MapCodec<? extends MapTemplateProcessor>> MAP_TEMPLATE_PROCESSOR_TYPE = register(PlasmidRegistryKeys.MAP_TEMPLATE_PROCESSOR_TYPE);

    private static <T> MappedRegistry<T> register(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.create(key).buildAndRegister();
    }

    public static void registerDynamicRegistries() {
        DynamicRegistries.register(PlasmidRegistryKeys.GAME_CONFIG, GameConfig.REGISTRY_CODEC);
        DynamicRegistries.register(PlasmidRegistryKeys.GAME_MENU_ENTRY, GameMenuEntryConfig.DIRECT_CODEC);
        DynamicRegistries.register(PlasmidRegistryKeys.GAME_MENU, GameMenuConfig.DIRECT_CODEC);
        DynamicRegistries.register(PlasmidRegistryKeys.GAME_MENU_THEME, GameMenuTheme.DIRECT_CODEC);
    }
}