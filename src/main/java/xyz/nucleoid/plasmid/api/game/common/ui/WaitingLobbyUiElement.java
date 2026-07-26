package xyz.nucleoid.plasmid.api.game.common.ui;

import java.util.List;
import java.util.SequencedCollection;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;

public interface WaitingLobbyUiElement {
    GuiElement createMainElement();

    default SequencedCollection<GuiElement> createExtendedElements() {
        return List.of(this.createMainElement());
    }

    static boolean isClick(ClickType type, SlotBasedGui gui) {
        return type.isRight || !(gui instanceof HotbarGui);
    }
}
