package xyz.nucleoid.plasmid.impl.menu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import xyz.nucleoid.plasmid.api.menu.*;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.impl.Plasmid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Works out what each loaded menu would be drawn with under each loaded theme, and reports whatever the two
 * cannot agree on: a menu asking for more room than the theme's chrome leaves it, or a layout that still
 * does not fit the region it got. Reported at datapack load rather than when a player happens to open the
 * menu; drawing degrades by dropping elements, never by crashing.
 * <p>
 * Menus written inline inside an entry are checked alongside the named ones. They appear in no registry, so
 * without walking into them, moving a menu inline would quietly cost it every check here.
 */
public final class GameMenuValidator {
    private GameMenuValidator() {
    }

    /**
     * Validates every loaded menu against the default theme, and in development against all of them.
     */
    public static void validateLoadedMenus(RegistryAccess registries) {
        boolean development = FabricLoader.getInstance().isDevelopmentEnvironment();

        // A menu reachable from two places is still only worth reporting once.
        Set<GameMenuConfig> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        registries.lookupOrThrow(PlasmidRegistryKeys.GAME_MENU).listElements().forEach(reference ->
                validate(registries, reference.key().identifier().toString(), reference.value(), development, seen));
    }

    private static void validate(RegistryAccess registries, String name, GameMenuConfig menu, boolean development, Set<GameMenuConfig> seen) {
        if (!seen.add(menu)) {
            return;
        }

        report(name, menu, GameMenuThemeTypes.DEFAULT.identifier().toString(), GameMenuThemeTypes.getDefault(registries));

        if (development) {
            registries.lookupOrThrow(PlasmidRegistryKeys.GAME_MENU_THEME).listElements().forEach(theme -> {
                if (!theme.key().equals(GameMenuThemeTypes.DEFAULT)) {
                    report(name, menu, theme.key().identifier().toString(), theme.value());
                }
            });

            // Sources expand through tags, so the entry count is not visible from the file alone.
            int sources = menu.entries().elements().size();
            int entries = menu.entries().elements().stream().mapToInt(set -> set.size()).sum();
            Plasmid.LOGGER.info("Game menu {} resolves {} source(s) to {} entries", name, sources, entries);
        }

        forEachInlineMenu(menu, nested ->
                validate(registries, name + " > " + nested.title().getString(), nested, development, seen));
    }

    /**
     * Named menus are reached through the registry on their own, so only inline ones are followed here.
     */
    private static void forEachInlineMenu(GameMenuConfig menu, Consumer<GameMenuConfig> consumer) {
        for (var sources : menu.entries().elements()) {
            for (var entry : sources) {
                entry.value().provideMenus(nested -> {
                    if (nested.kind() == Holder.Kind.DIRECT) {
                        consumer.accept(nested.value());
                    }
                });
            }
        }
    }

    private static void report(String menu, GameMenuConfig config, String themeName, GameMenuTheme theme) {
        var problems = new ArrayList<String>();

        // Resolving is what surfaces a menu asking for more than the theme can leave it; the layout then
        // reports whatever still does not fit the region it actually got. There is no opening menu here, so
        // an inheriting menu is checked at the size it would get on its own, the smallest it can be.
        var frame = GameMenuFrame.resolve(theme, theme.baseInsets(), config.size(), config.entries(), null, problems::add);
        problems.addAll(((GameMenuLayout<?>) config.entries()).validate(frame));

        for (var problem : problems) {
            Plasmid.LOGGER.warn("Game menu {} is incompatible with theme {}: it {}", menu, themeName, problem);
        }
    }
}
