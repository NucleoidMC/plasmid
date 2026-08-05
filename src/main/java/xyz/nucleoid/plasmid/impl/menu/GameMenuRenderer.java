package xyz.nucleoid.plasmid.impl.menu;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.menu.GameMenu;
import xyz.nucleoid.plasmid.api.menu.GameMenuTheme;
import xyz.nucleoid.plasmid.api.menu.GameMenuThemeTypes;

import java.util.Map;
import java.util.UUID;

/**
 * Opens {@link GameMenu}s. A menu opened while another is on screen becomes its child, which is what gives
 * it a back button and makes it inherit the parent's theme; anything else starts a fresh chain.
 */
public final class GameMenuRenderer {
    /**
     * Forced themes, held only for the length of one {@link #openWithTheme} call.
     */
    private static final Map<UUID, GameMenuTheme> FORCED = new Object2ObjectOpenHashMap<>();

    private GameMenuRenderer() {
    }

    public static void open(GameMenu menu, ServerPlayer player) {
        open(menu, player, GameMenuContextImpl.openFor(player));
    }

    /**
     * Runs {@code action} with whatever menu it opens forced into {@code theme}, which everything opened
     * from that menu then inherits.
     * <p>
     * Scoped around the action rather than taking the menu, because what an entry does when clicked is its
     * own business: {@code plasmid:menu} opens a named menu, a hybrid picks one of two, and a game can build
     * one in code and never hand it over. Anything that wants to preview a theme would otherwise only work
     * for the first of those.
     * <p>
     * Covers what opens before this returns, which is every menu-opening entry there is. An entry opening
     * one later, off a future tick, would get the ordinary theme.
     */
    public static void openWithTheme(ServerPlayer player, GameMenuTheme theme, Runnable action) {
        var previous = FORCED.put(player.getUUID(), theme);

        try {
            action.run();
        } finally {
            if (previous != null) {
                FORCED.put(player.getUUID(), previous);
            } else {
                FORCED.remove(player.getUUID());
            }
        }
    }

    static void open(GameMenu menu, ServerPlayer player, @Nullable GameMenuContextImpl parent) {
        // A forced theme beats the menu's own choice: previewing a theme is worth nothing if the menus that
        // name one are the ones it cannot show.
        var forced = FORCED.get(player.getUUID());
        var theme = forced != null
                ? forced
                : GameMenuThemeTypes.resolve(player.registryAccess(), menu.themeOverride(), parent == null ? null : parent.theme());

        new GameMenuContextImpl(player, menu, theme, parent).open();
    }

    /**
     * Reopens a menu already visited, rebuilt so counts are current but keeping the theme it resolved to.
     * <p>
     * Re-resolving would lose anything not derivable from the menu itself: a theme forced at open time, or
     * one inherited from a chain the rebuilt context no longer has above it.
     */
    static void reopen(GameMenuContextImpl context) {
        new GameMenuContextImpl(context.player(), context.menu(), context.theme(), context.parent()).open();
    }
}
