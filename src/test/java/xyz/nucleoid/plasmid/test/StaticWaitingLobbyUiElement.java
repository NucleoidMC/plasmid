package xyz.nucleoid.plasmid.test;

import eu.pb4.sgui.api.elements.GuiElement;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;

import java.util.List;
import java.util.SequencedCollection;

public record StaticWaitingLobbyUiElement(GuiElement mainElement, SequencedCollection<GuiElement> extendedElements) implements WaitingLobbyUiElement {
    public StaticWaitingLobbyUiElement(GuiElement element) {
        this(element, List.of(element));
    }

    @Override
    public GuiElement createMainElement() {
        return this.mainElement;
    }

    @Override
    public SequencedCollection<GuiElement> createExtendedElements() {
        return this.extendedElements;
    }
}
