package xyz.nucleoid.plasmid.api.game.common.ui;

import java.util.List;
import java.util.SequencedCollection;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;

public interface WaitingLobbyUiElement {
    GuiElementInterface createMainElement();

    default SequencedCollection<GuiElementInterface> createExtendedElements() {
        return List.of(this.createMainElement());
    }

    static boolean isClick(ClickType type, SlotGuiInterface gui) {
        return type.isRight || !(gui instanceof HotbarGui);
    }
}
