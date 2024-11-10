package xyz.nucleoid.plasmid.game.common.ui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.HotbarGui;
import xyz.nucleoid.plasmid.game.common.ui.element.WaitingLobbyUiElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class WaitingLobbyUiLayout {
    private static final int SIZE = 9;

    private final Consumer<GuiElementInterface[]> callback;

    private final List<WaitingLobbyUiElement> leftElements = new ArrayList<>();
    private final List<WaitingLobbyUiElement> rightElements = new ArrayList<>();

    private WaitingLobbyUiLayout(Consumer<GuiElementInterface[]> callback) {
        this.callback = callback;
    }

    public void addLeft(WaitingLobbyUiElement element) {
        this.add(element, this.leftElements);
    }

    public void addRight(WaitingLobbyUiElement element) {
        this.add(element, this.rightElements);
    }

    private void add(WaitingLobbyUiElement element, List<WaitingLobbyUiElement> elements) {
        Objects.requireNonNull(element);

        if (this.leftElements.contains(element) || this.rightElements.contains(element)) {
            throw new IllegalArgumentException("Element " + element + " has already been added to the layout");
        } else if (this.leftElements.size() + this.rightElements.size() >= SIZE) {
            throw new IllegalStateException("Cannot have more than " + SIZE + " elements in the layout");
        }

        elements.add(element);
    }

    private GuiElementInterface[] build() {
        var elements = new GuiElementInterface[SIZE];
        Arrays.fill(elements, GuiElement.EMPTY);

        if (this.leftElements.isEmpty() && this.rightElements.isEmpty()) {
            return elements;
        }

        var elementsToEntries = new HashMap<WaitingLobbyUiElement, WaitingLobbyUiLayoutEntry>(this.leftElements.size() + this.rightElements.size());

        for (var element : this.leftElements) {
            elementsToEntries.put(element, new WaitingLobbyUiLayoutEntry(element));
        }

        for (var element : this.rightElements) {
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

        for (var element : this.leftElements) {
            var entry = elementsToEntries.get(element);

            for (var guiElement : entry.getGuiElements()) {
                elements[index] = guiElement;
                index += 1;
            }
        }

        index = SIZE - 1;

        for (var element : this.rightElements) {
            var entry = elementsToEntries.get(element);

            for (var guiElement : entry.getGuiElements()) {
                elements[index] = guiElement;
                index -= 1;
            }
        }

        return elements;
    }

    public void refresh() {
        this.callback.accept(this.build());
    }

    @Override
    public String toString() {
        return "WaitingLobbyUiLayout{leftElements=" + this.leftElements + ", rightElements=" + this.rightElements + "}";
    }

    public static WaitingLobbyUiLayout of(Consumer<GuiElementInterface[]> callback) {
        return new WaitingLobbyUiLayout(callback);
    }
}
