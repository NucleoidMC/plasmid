package xyz.nucleoid.plasmid.api.menu;

import eu.pb4.sgui.api.elements.GuiElement;

/**
 * A single cell of a {@link GameMenuLayout}.
 */
public sealed interface GameMenuElement {
    GameMenuElement EMPTY = new Empty();

    static GameMenuElement of(GameMenuEntry entry) {
        return new Entry(entry);
    }

    static GameMenuElement of(GuiElement element) {
        return new Raw(element);
    }

    static GameMenuElement of(GameMenuFeature feature) {
        return new Feature(feature);
    }

    /**
     * A selectable entry, rendered through {@link GameMenuTheme#entryElement}.
     */
    record Entry(GameMenuEntry entry) implements GameMenuElement {
    }

    /**
     * Theme-owned chrome pinned here instead of being positioned by the theme.
     */
    record Feature(GameMenuFeature feature) implements GameMenuElement {
    }

    /**
     * A pre-built element, passed through untouched.
     */
    record Raw(GuiElement element) implements GameMenuElement {
    }

    /**
     * An intentional hole.
     */
    record Empty() implements GameMenuElement {
    }
}
