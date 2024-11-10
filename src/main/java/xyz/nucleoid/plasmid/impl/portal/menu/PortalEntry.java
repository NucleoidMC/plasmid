package xyz.nucleoid.plasmid.impl.portal.menu;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.GamePortal;

public record PortalEntry(
        GamePortal portal,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    public PortalEntry(GamePortal portal) {
        this(portal, portal.getName(), portal.getDescription(), portal.getIcon());
    }

    @Override
    public void click(ServerPlayerEntity player, boolean alt) {
        this.portal.requestJoin(player, alt);
    }

    @Override
    public int getPlayerCount() {
        return this.portal.getPlayerCount();
    }

    @Override
    public int getSpectatorCount() {
        return this.portal.getSpectatorCount();
    }

    @Override
    public int getMaxPlayerCount() {
        return this.portal.getMaxPlayerCount();
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        portal.provideGameSpaces(consumer);
    }

    @Override
    public GamePortalBackend.ActionType getActionType() {
        return this.portal.getBackend().getActionType();
    }

    @Override
    public GamePortalBackend.ActionType getAltActionType() {
        return this.portal.getBackend().getAltActionType();
    }
}