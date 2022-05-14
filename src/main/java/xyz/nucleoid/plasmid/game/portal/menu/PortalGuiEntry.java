package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortal;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;
import xyz.nucleoid.plasmid.util.Guis;

public record PortalGuiEntry(
        GamePortal portal,
        GuiProvider provider,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayerEntity player, CompletableFuture<GameSpace> future) {
        var ui = Guis.createSelectorGui(player, this.name.shallowCopy(), true, this.provider.getGuiElements(future));
        ui.open();
    }

    @Override
    public int getPlayerCount() {
        return this.portal.getPlayerCount();
    }
}