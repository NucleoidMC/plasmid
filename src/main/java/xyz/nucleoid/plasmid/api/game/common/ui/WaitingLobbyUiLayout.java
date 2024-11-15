package xyz.nucleoid.plasmid.api.game.common.ui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import xyz.nucleoid.plasmid.impl.game.common.ui.WaitingLobbyUiLayoutImpl;

import java.util.Objects;
import java.util.function.Consumer;

public interface WaitingLobbyUiLayout {
    void addLeading(WaitingLobbyUiElement element);

    void addTrailing(WaitingLobbyUiElement element);

    void refresh();

    static WaitingLobbyUiLayout of(Consumer<GuiElementInterface[]> callback) {
        return new WaitingLobbyUiLayoutImpl(Objects.requireNonNull(callback));
    }
}
