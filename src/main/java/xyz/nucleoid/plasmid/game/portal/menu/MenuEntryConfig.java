package xyz.nucleoid.plasmid.game.portal.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.Optional;
import java.util.function.Function;

public interface MenuEntryConfig {
    TinyRegistry<Codec<? extends MenuEntryConfig>> REGISTRY = TinyRegistry.create();

    Codec<MenuEntryConfig> CODEC_OBJECT = REGISTRY.dispatchStable(MenuEntryConfig::codec, Function.identity());
    Codec<MenuEntryConfig> CODEC = Codec.either(GameConfig.CODEC, CODEC_OBJECT).xmap(either -> {
        return either.map((game) -> new GameMenuEntryConfig(game, Optional.empty(), Optional.empty(), Optional.empty()), Function.identity());
    }, Either::right);

    static void register(Identifier key, Codec<? extends MenuEntryConfig> codec) {
        REGISTRY.register(key, codec);
    }

    MenuEntry createEntry();

    Codec<? extends MenuEntryConfig> codec();
}
