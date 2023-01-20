package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortal;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;
import xyz.nucleoid.plasmid.util.Guis;

@Deprecated(forRemoval = true)
public record PortalGuiEntry(
        GamePortal portal,
        GuiProvider provider,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayerEntity player) {
        var ui = Guis.createSelectorGui(player, this.name.copy(), true, this.provider.getGuiElements());
        ui.open();
    }

    @Override
    public int getPlayerCount() {
        return this.portal.getPlayerCount();
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        this.portal.provideGameSpaces(consumer);
    }
}
