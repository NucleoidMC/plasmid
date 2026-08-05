package xyz.nucleoid.plasmid.api.menu;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.menu.SimpleGameMenuTheme;

import java.util.Optional;

/**
 * Registration and lookup of {@link GameMenuTheme}s.
 * <p>
 * Types are registered here in code; instances live in the {@code plasmid:game_menu_theme} dynamic registry,
 * at {@code data/<namespace>/plasmid/game_menu_theme/*.json}. The instance under {@link #DEFAULT} is the
 * server default; Plasmid ships a plain one, and any datapack writing that path replaces it.
 */
public final class GameMenuThemeTypes {
    /**
     * The instance used when nothing more specific applies.
     */
    public static final ResourceKey<GameMenuTheme> DEFAULT = ResourceKey.create(PlasmidRegistryKeys.GAME_MENU_THEME, Plasmid.id("default"));

    /**
     * Configured entirely from JSON: content region, spacing, filler, feature placement.
     */
    public static final MapCodec<? extends GameMenuTheme> SIMPLE = register("simple", SimpleGameMenuTheme.CODEC);

    private GameMenuThemeTypes() {
    }

    public static MapCodec<? extends GameMenuTheme> register(Identifier key, MapCodec<? extends GameMenuTheme> codec) {
        return Registry.register(PlasmidRegistries.GAME_MENU_THEME_TYPE, key, codec);
    }

    private static MapCodec<? extends GameMenuTheme> register(String key, MapCodec<? extends GameMenuTheme> codec) {
        return register(Plasmid.id(key), codec);
    }

    /**
     * The server default, falling back to a built-in plain theme if {@link #DEFAULT} is missing, so menus
     * open even on a broken data setup.
     */
    public static GameMenuTheme getDefault(RegistryAccess registries) {
        return registries.lookupOrThrow(PlasmidRegistryKeys.GAME_MENU_THEME)
                .get(DEFAULT)
                .<GameMenuTheme>map(Holder::value)
                .orElse(SimpleGameMenuTheme.DEFAULT);
    }

    /**
     * Picks a theme: the menu's own override, then the theme of the menu it was opened from, then the server
     * default. Theming travels screen to screen, so there is no per-player state to clean up.
     *
     * @param inherited the opening menu's theme, or {@code null} if this starts a fresh chain
     */
    public static GameMenuTheme resolve(RegistryAccess registries, Optional<Holder<GameMenuTheme>> override, @Nullable GameMenuTheme inherited) {
        if (override.isPresent()) {
            return override.get().value();
        }

        return inherited != null ? inherited : getDefault(registries);
    }
}
