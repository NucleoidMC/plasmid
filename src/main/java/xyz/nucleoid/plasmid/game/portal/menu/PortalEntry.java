package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortal;

public record PortalEntry(
        GamePortal portal,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayerEntity player, CompletableFuture<GameSpace> future) {
        this.portal.requestJoin(player);
    }

    @Override
    public int getPlayerCount() {
        return this.portal.getPlayerCount();
    }
}