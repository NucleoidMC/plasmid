package xyz.nucleoid.plasmid.test;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;

import java.util.List;
import java.util.SequencedCollection;

public record StaticWaitingLobbyUiElement(GuiElementInterface mainElement, SequencedCollection<GuiElementInterface> extendedElements) implements WaitingLobbyUiElement {
    public StaticWaitingLobbyUiElement(GuiElementInterface element) {
        this(element, List.of(element));
    }

    @Override
    public GuiElementInterface createMainElement() {
        return this.mainElement;
    }

    @Override
    public SequencedCollection<GuiElementInterface> createExtendedElements() {
        return this.extendedElements;
    }
}
