package xyz.nucleoid.plasmid.api.menu;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.plasmid.impl.Plasmid;

/**
 * The features Plasmid drives itself.
 */
public final class GameMenuFeatures {
    /**
     * Switches between the menu contents and the games currently open behind it. Added to any menu holding a
     * {@link GameMenuEntry#isGameEntry() game entry}; declare an explicit feature set to suppress it.
     */
    public static final GameMenuFeature GAME_FILTER = register(Plasmid.id("game_filter"), new GameMenuFeature(
            new GameMenuFeature.Appearance(
                    Component.translatable("text.plasmid.ui.game_menu.all_games"),
                    new ItemStackTemplate(Items.LANTERN)),
            new GameMenuFeature.Appearance(
                    Component.translatable("text.plasmid.ui.game_menu.open_games"),
                    new ItemStackTemplate(Items.SOUL_LANTERN))));

    /**
     * Returns to the opening menu. Added whenever {@link GameMenuContext#canGoBack()} holds.
     */
    public static final GameMenuFeature BACK = register("back",
            Component.translatable("gui.back"),
            new ItemStackTemplate(Items.STRUCTURE_VOID));

    /**
     * Closes the menu outright. Always available, since any menu can be closed, so whether a button appears
     * is purely a matter of the theme placing it. {@link GameMenuFeatureLayout#standard} does not.
     */
    public static final GameMenuFeature CLOSE = register("close",
            Component.translatable("gui.done"),
            new ItemStackTemplate(Items.BARRIER));

    /**
     * Page controls, added as a pair when the contents overflow the content region. A
     * {@link xyz.nucleoid.plasmid.api.menu.layout.FixedLayout} never paginates, so never gains them.
     */
    public static final GameMenuFeature PREVIOUS_PAGE = register("previous_page",
            Component.translatable("spectatorMenu.previous_page"),
            new ItemStackTemplate(Items.PLAYER_HEAD)); //TODO

    public static final GameMenuFeature NEXT_PAGE = register("next_page",
            Component.translatable("spectatorMenu.next_page"),
            new ItemStackTemplate(Items.PLAYER_HEAD)); //TODO

    private GameMenuFeatures() {
    }

    public static GameMenuFeature register(Identifier key, GameMenuFeature feature) {
        return Registry.register(PlasmidRegistries.GAME_MENU_FEATURE, key, feature);
    }

    private static GameMenuFeature register(String key, Component name, ItemStackTemplate icon) {
        return register(Plasmid.id(key), new GameMenuFeature(name, icon));
    }
}
