package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.on_demand.OnDemandGame;

public record GameMenuEntry(
        OnDemandGame game,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayerEntity player, CompletableFuture<GameSpace> future) {
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