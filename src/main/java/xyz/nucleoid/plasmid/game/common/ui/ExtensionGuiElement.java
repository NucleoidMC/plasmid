package xyz.nucleoid.plasmid.game.common.ui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import xyz.nucleoid.plasmid.game.common.ui.element.WaitingLobbyUiElement;
import xyz.nucleoid.plasmid.util.Guis;

public record ExtensionGuiElement(GuiElementInterface delegate, WaitingLobbyUiLayoutEntry entry) implements GuiElementInterface {
    @Override
    public ItemStack getItemStack() {
        return this.delegate.getItemStack();
    }

    @Override
    public ClickCallback getGuiCallback() {
        return (index, type, action, gui) -> {
            if (WaitingLobbyUiElement.isClick(type, gui)) {
                this.openExtendedGui(gui);
            }
        };
    }

    private void openExtendedGui(SlotGuiInterface parent) {
        var player = parent.getPlayer();
        var name = this.delegate.getItemStackForDisplay(parent).getName().copy();

        var ui = Guis.createSelectorGui(player, name, true, () -> {
            // Refresh elements
            this.openExtendedGui(parent);
        }, () -> {
            parent.open();
        }, this.entry.getElement().createExtendedElements());

        ui.open();
    }
}
