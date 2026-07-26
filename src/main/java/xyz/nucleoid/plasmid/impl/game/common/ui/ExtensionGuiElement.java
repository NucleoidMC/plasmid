package xyz.nucleoid.plasmid.impl.game.common.ui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiElement;
import xyz.nucleoid.plasmid.api.util.Guis;

public record ExtensionGuiElement(GuiElement delegate, WaitingLobbyUiLayoutEntry entry) implements GuiElement {
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

    private void openExtendedGui(SlotBasedGui parent) {
        var player = parent.getPlayer();
        var name = this.delegate.getItemStackForDisplay(parent).getHoverName().copy();

        var ui = Guis.createSelectorGui(player, name, true, gui -> {
            if (gui.isOpen()) {
                // Refresh elements
                this.openExtendedGui(parent);
            }
        }, gui -> {
            parent.open();
        }, this.entry.getElement().createExtendedElements());

        ui.open();
    }
}
