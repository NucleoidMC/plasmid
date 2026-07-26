package xyz.nucleoid.plasmid.impl.game.common.ui.element;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;

public class LeaveGameWaitingLobbyUiElement implements WaitingLobbyUiElement {
    private final GameSpace gameSpace;
    private ServerPlayer player;

    public LeaveGameWaitingLobbyUiElement(GameSpace gameSpace, ServerPlayer player) {
        this.gameSpace = gameSpace;
        this.player = player;
    }

    @Override
    public GuiElement createMainElement() {
        return new GuiElementBuilder(Items.BED.red())
                .setItemName(Component.translatable("text.plasmid.game.waiting_lobby.leave_game"))
                .setCallback((index, type, action, gui) -> {
                    if (WaitingLobbyUiElement.isClick(type, gui)) {
                        this.gameSpace.getPlayers().kick(this.player);
                    }
                })
                .build();
    }
}
