package xyz.nucleoid.plasmid.api.registry;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProviderType;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalConfig;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuEntryConfig;

public class PlasmidRegistries {
    public static final Registry<GameType<?>> GAME_TYPE = register(PlasmidRegistryKeys.GAME_TYPE);
    public static final Registry<MapCodec<? extends GamePortalConfig>> GAME_PORTAL_CONFIG = register(PlasmidRegistryKeys.GAME_PORTAL_CONFIG);
    public static final Registry<MapCodec<? extends MenuEntryConfig>> MENU_ENTRY = register(PlasmidRegistryKeys.MENU_ENTRY);
    public static final Registry<TeamListProviderType<?>> TEAM_LIST_PROVIDER_TYPE = register(PlasmidRegistryKeys.TEAM_LIST_PROVIDER_TYPE);

    private static <T> SimpleRegistry<T> register(RegistryKey<Registry<T>> key) {
        return FabricRegistryBuilder.createSimple(key).buildAndRegister();
    }

    public static void registerDynamicRegistries() {
        DynamicRegistries.register(PlasmidRegistryKeys.GAME_CONFIG, GameConfig.REGISTRY_CODEC);
    }
}