package xyz.nucleoid.plasmid.game.common.ui.element;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;

public class LeaveGameWaitingLobbyUiElement implements WaitingLobbyUiElement {
    private final GameSpace gameSpace;
    private ServerPlayerEntity player;

    public LeaveGameWaitingLobbyUiElement(GameSpace gameSpace, ServerPlayerEntity player) {
        this.gameSpace = gameSpace;
        this.player = player;
    }

    @Override
    public GuiElementInterface createMainElement() {
        return new GuiElementBuilder(Items.RED_BED)
                .setItemName(Text.translatable("text.plasmid.game.waiting_lobby.leave_game"))
                .setCallback((index, type, action, gui) -> {
                    if (WaitingLobbyUiElement.isClick(type, gui)) {
                        this.gameSpace.getPlayers().kick(this.player);
                    }
                })
                .build();
    }
}
