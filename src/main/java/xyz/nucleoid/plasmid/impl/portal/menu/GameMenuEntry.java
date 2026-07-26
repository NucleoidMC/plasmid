package xyz.nucleoid.plasmid.impl.portal.menu;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.game.ConcurrentGamePortalBackend;

public record GameMenuEntry(
        ConcurrentGamePortalBackend game,
        Component name,
        List<Component> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(ServerPlayer player, boolean alt) {
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