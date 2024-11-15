package xyz.nucleoid.plasmid.impl.game.common.ui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class WaitingLobbyUiLayoutImpl implements WaitingLobbyUiLayout {
    private static final int SIZE = 9;

    private final Consumer<GuiElementInterface[]> callback;

    private final List<WaitingLobbyUiElement> leadingElements = new ArrayList<>();
    private final List<WaitingLobbyUiElement> trailingElements = new ArrayList<>();

    public WaitingLobbyUiLayoutImpl(Consumer<GuiElementInterface[]> callback) {
        this.callback = callback;
    }

    @Override
    public void addLeading(WaitingLobbyUiElement element) {
        this.add(element, this.leadingElements);
    }

    @Override
    public void addTrailing(WaitingLobbyUiElement element) {
        this.add(element, this.trailingElements);
    }

    private void add(WaitingLobbyUiElement element, List<WaitingLobbyUiElement> elements) {
        Objects.requireNonNull(element);

        if (this.leadingElements.contains(element) || this.trailingElements.contains(element)) {
            throw new IllegalArgumentException("Element " + element + " has already been added to the layout");
        } else if (this.leadingElements.size() + this.trailingElements.size() >= SIZE) {
            throw new IllegalStateException("Cannot have more than " + SIZE + " elements in the layout");
        }

        elements.add(element);
    }

    private GuiElementInterface[] build() {
        var elements = new GuiElementInterface[SIZE];
        Arrays.fill(elements, GuiElement.EMPTY);

        if (this.leadingElements.isEmpty() && this.trailingElements.isEmpty()) {
            return elements;
        }

        var elementsToEntries = new HashMap<WaitingLobbyUiElement, WaitingLobbyUiLayoutEntry>(this.leadingElements.size() + this.trailingElements.size());

        for (var element : this.leadingElements) {
            elementsToEntries.put(element, new WaitingLobbyUiLayoutEntry(element));
        }

        for (var element : this.trailingElements) {
            elementsToEntries.put(element, new WaitingLobbyUiLayoutEntry(element));
        }

        var entries = new ArrayList<>(elementsToEntries.values());
        entries.sort(Comparator.comparingInt(WaitingLobbyUiLayoutEntry::size));

        int shrinkIndex = 0;

        while (WaitingLobbyUiLayoutEntry.getTotalSize(entries) > SIZE) {
            var entry = entries.get(shrinkIndex);
            entry.shrink();

            shrinkIndex += 1;
        }

        int index = 0;

        for (var element : this.leadingElements) {
            var entry = elementsToEntries.get(element);

            for (var guiElement : entry.getGuiElements()) {
                elements[index] = guiElement;
                index += 1;
            }
        }

        index = SIZE - 1;

        for (var element : this.trailingElements) {
            var entry = elementsToEntries.get(element);

            for (var guiElement : entry.getGuiElements()) {
                elements[index] = guiElement;
                index -= 1;
            }
        }

        return elements;
    }

    @Override
    public void refresh() {
        this.callback.accept(this.build());
    }

    @Override
    public String toString() {
        return "WaitingLobbyUiLayoutImpl{leadingElements=" + this.leadingElements + ", trailingElements=" + this.trailingElements + "}";
    }
}
