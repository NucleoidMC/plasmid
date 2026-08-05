package xyz.nucleoid.plasmid.api.menu;

import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * The live state of one open {@link GameMenu}, passed to every {@link GameMenuTheme} method.
 */
public interface GameMenuContext {
    ServerPlayer player();

    GameMenu menu();

    GameMenuTheme theme();

    /**
     * The geometry being drawn with, worked out once when the menu opened from its own
     * {@link GameMenu#sizeRule()} and the theme's {@link GameMenuTheme#insets}.
     *
     * <p>A container cannot be resized while it is on screen, so this holds for the life of the menu. The
     * open-games view pages within whatever the menu's own contents earned.
     */
    GameMenuFrame frame();

    /**
     * The features in effect: the menu's own, plus {@link GameMenuFeatures#CLOSE}, then
     * {@link GameMenuFeatures#BACK} when there is somewhere to go back to and the page controls when the
     * contents overflow.
     *
     * <p>Being in effect is not being drawn. A feature the theme's {@link GameMenuTheme#featureLayout}
     * places nowhere is not offered.
     */
    Set<GameMenuFeature> features();

    default boolean hasFeature(GameMenuFeature feature) {
        return this.features().contains(feature);
    }

    /**
     * Whether the layout placed this feature itself, in which case the theme must not place it again.
     */
    boolean isPinned(GameMenuFeature feature);

    /**
     * Whether a toggle feature is engaged. For {@link GameMenuFeatures#GAME_FILTER} that means the
     * open-games view is showing. Lets a theme pick {@link GameMenuFeature#appearance} without knowing what
     * the toggle does.
     */
    boolean isActive(GameMenuFeature feature);

    View view();

    void setView(View view);

    int page();

    int pageCount();

    void setPage(int page);

    /**
     * Redraws in place.
     */
    void refresh();

    /**
     * Whether this menu was opened from another that is still on screen. Derived from the open menu rather
     * than remembered state, so a menu opened from a sign or a command never has a back button.
     */
    boolean canGoBack();

    /**
     * Reopens the menu this was opened from, refreshed. Does nothing if there is none.
     */
    void goBack();

    enum View {
        /**
         * The menu's own contents.
         */
        CONTENT,
        /**
         * Games currently open behind it, shown while {@link GameMenuFeatures#GAME_FILTER} is engaged.
         */
        OPEN_GAMES
    }
}
