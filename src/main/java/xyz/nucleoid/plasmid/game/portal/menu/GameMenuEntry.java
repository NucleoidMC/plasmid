package xyz.nucleoid.plasmid.game.portal.menu;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.ListedGameSpace;
import xyz.nucleoid.plasmid.game.portal.on_demand.OnDemandGame;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record GameMenuEntry(
        OnDemandGame game,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayerEntity player, CompletableFuture<ListedGameSpace> future) {
        game.getOrOpen(player.getServer()).handle((gameSpace, throwable) -> {
            if (throwable == null) {
                future.complete(gameSpace);
            } else {
                future.completeExceptionally(throwable);
            }
            return null;
        });
    }

    @Override
    public int getPlayerCount() {
        return this.game.getPlayerCount();
    }
}
