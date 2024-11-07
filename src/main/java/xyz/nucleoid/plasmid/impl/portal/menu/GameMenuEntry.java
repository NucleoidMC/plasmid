package xyz.nucleoid.plasmid.impl.portal.menu;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.game.ConcurrentGamePortalBackend;

public record GameMenuEntry(
        ConcurrentGamePortalBackend game,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayerEntity player, boolean alt) {
        game.applyTo(player, alt);
    }

    @Override
    public int getPlayerCount() {
        return this.game.getPlayerCount();
    }

    @Override
    public GamePortalBackend.ActionType getActionType() {
        return this.game.getActionType();
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        game.provideGameSpaces(consumer);
    }
}