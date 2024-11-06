package xyz.nucleoid.plasmid.game.common.ui.element;

import java.util.SequencedCollection;

import eu.pb4.sgui.api.elements.GuiElementInterface;

public interface WaitingLobbyUiElement {
    GuiElementInterface createMainElement();

    SequencedCollection<GuiElementInterface> createExtendedElements();
}
