package xyz.nucleoid.plasmid.impl.portal.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.portal.menu.MenuEntryConfigs;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.Identifier;

public interface MenuEntryConfig {
    Codec<MenuEntryConfig> CODEC_OBJECT = PlasmidRegistries.MENU_ENTRY.byNameCodec().dispatchStable(MenuEntryConfig::codec, Function.identity());
    Codec<MenuEntryConfig> CODEC = Codec.either(GameConfig.ENTRY_CODEC, CODEC_OBJECT).xmap(either -> {
        return either.map((game) -> new GameMenuEntryConfig(game, Optional.empty(), Optional.empty(), Optional.empty()), Function.identity());
    }, Either::right);

    MenuEntry createEntry();

    MapCodec<? extends MenuEntryConfig> codec();
}
