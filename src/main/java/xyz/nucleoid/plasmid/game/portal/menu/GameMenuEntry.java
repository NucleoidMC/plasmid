package xyz.nucleoid.plasmid.game.portal.menu;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.game.ConcurrentGamePortalBackend;

public record GameMenuEntry(
        ConcurrentGamePortalBackend game,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayerEntity player) {
        game.applyTo(player);
    }

    @Override
    public int getPlayerCount() {
        return this.game.getPlayerCount();
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        game.provideGameSpaces(consumer);
    }
}