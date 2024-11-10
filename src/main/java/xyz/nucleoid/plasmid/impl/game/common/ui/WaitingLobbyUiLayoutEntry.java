package xyz.nucleoid.plasmid.impl.game.common.ui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;

import java.util.List;
import java.util.SequencedCollection;

class WaitingLobbyUiLayoutEntry {
    private final WaitingLobbyUiElement element;

    private SequencedCollection<GuiElementInterface> guiElements;

    protected WaitingLobbyUiLayoutEntry(WaitingLobbyUiElement element) {
        this.element = element;

        this.guiElements = element.createExtendedElements();
    }

    public WaitingLobbyUiElement getElement() {
        return this.element;
    }

    public SequencedCollection<GuiElementInterface> getGuiElements() {
        return this.guiElements;
    }

    public void shrink() {
        if (this.guiElements.size() > 1) {
            var element = new ExtensionGuiElement(this.element.createMainElement(), this);
            this.guiElements = List.of(element);
        }
    }

    public int size() {
        return this.guiElements.size();
    }

    @Override
    public String toString() {
        return "WaitingLobbyUiLayoutEntry{element=" + element + ", guiElements=" + guiElements + "}";
    }

    protected static int getTotalSize(Iterable<WaitingLobbyUiLayoutEntry> entries) {
        int size = 0;

        for (var entry : entries) {
            size += entry.size();
        }

        return size;
    }
}
