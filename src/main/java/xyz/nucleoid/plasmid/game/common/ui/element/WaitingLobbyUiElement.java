package xyz.nucleoid.plasmid.game.common.ui.element;

import java.util.SequencedCollection;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;

public interface WaitingLobbyUiElement {
    GuiElementInterface createMainElement();

    SequencedCollection<GuiElementInterface> createExtendedElements();

    static boolean isClick(ClickType type, SlotGuiInterface gui) {
        return type.isRight || !(gui instanceof HotbarGui);
    }
}
