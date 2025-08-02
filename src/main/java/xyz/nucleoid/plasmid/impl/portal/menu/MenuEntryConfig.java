package xyz.nucleoid.plasmid.impl.portal.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.portal.menu.MenuEntryConfigs;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;

import java.util.Optional;
import java.util.function.Function;

public interface MenuEntryConfig {
    /**
     * @deprecated Use {@link PlasmidRegistries#MENU_ENTRY} instead.
     */
    @Deprecated
    Registry<MapCodec<? extends MenuEntryConfig>> REGISTRY = PlasmidRegistries.MENU_ENTRY;

    Codec<MenuEntryConfig> CODEC_OBJECT = PlasmidRegistries.MENU_ENTRY.getCodec().dispatchStable(MenuEntryConfig::codec, Function.identity());
    Codec<MenuEntryConfig> CODEC = Codec.either(GameConfig.ENTRY_CODEC, CODEC_OBJECT).xmap(either -> {
        return either.map((game) -> new GameMenuEntryConfig(game, Optional.empty(), Optional.empty(), Optional.empty()), Function.identity());
    }, Either::right);

    /**
     * @deprecated Use {@link MenuEntryConfigs#register(Identifier, MapCodec)} instead.
     */
    @Deprecated
    static MapCodec<? extends MenuEntryConfig> register(Identifier key, MapCodec<? extends MenuEntryConfig> codec) {
        return MenuEntryConfigs.register(key, codec);
    }

    MenuEntry createEntry();

    MapCodec<? extends MenuEntryConfig> codec();
}
