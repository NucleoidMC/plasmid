package xyz.nucleoid.plasmid.impl.game.common.ui.element;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class ReadyGameWaitingLobbyUiElement implements WaitingLobbyUiElement {
    private final WaitingLobbyUiLayout layout;
    private final Consumer<Boolean> callback;
    private final GameSpace gameSpace;
    private final int minPlayers;
    public boolean hasVoted = false;
    public ReadyGameWaitingLobbyUiElement(WaitingLobbyUiLayout layout, GameSpace gameSpace, int minPlayers, Consumer<Boolean> callback) {
        this.layout = layout;
        this.gameSpace = gameSpace;
        this.minPlayers = minPlayers;
        this.callback = callback;
    }

    @Override
    public GuiElement createMainElement() {
        if (this.gameSpace.getPlayers().participants().size() < this.minPlayers) {

            return new GuiElementBuilder(Items.AIR).build();
        }
        return new GuiElementBuilder(!this.hasVoted ? Items.WOOL.green() : Items.WOOL.red() )
                .setItemName(Component.translatable("text.plasmid.game.waiting_lobby.player_vote_item"))
                .setCallback((index, type, action, gui) -> {
                    if (WaitingLobbyUiElement.isClick(type, gui)) {
                        this.hasVoted = !this.hasVoted;
                        this.callback.accept(this.hasVoted);
                        this.layout.refresh();
                    }
                })
                .build();
    }
}
