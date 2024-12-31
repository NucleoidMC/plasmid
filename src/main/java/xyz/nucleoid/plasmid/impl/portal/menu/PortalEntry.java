package xyz.nucleoid.plasmid.impl.portal.menu;

import java.util.List;
import java.util.function.Consumer;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.impl.portal.backend.PortalUserContext;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;
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
    public void click(PortalUserContext context, ClickType type) {
        this.portal.applyTo(context, type);
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
    public List<GamePortalBackend.Action> getActions(PortalUserContext context) {
        return this.portal.getBackend().getActions(context);
    }
}