package xyz.nucleoid.plasmid.impl.portal.menu;

import java.util.List;
import java.util.function.Consumer;

import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.impl.portal.backend.PortalUserContext;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.backend.game.ConcurrentGamePortalBackend;

public record GameMenuEntry(
        ConcurrentGamePortalBackend game,
        Text name,
        List<Text> description,
        ItemStack icon
) implements MenuEntry {
    @Override
    public void click(PortalUserContext context, ClickType type) {
        game.applyTo(context, type);
    }

    @Override
    public int getPlayerCount() {
        return this.game.getPlayerCount();
    }

    @Override
    public List<GamePortalBackend.Action> getActions(PortalUserContext context) {
        return this.game.getActions(context);
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        game.provideGameSpaces(consumer);
    }
}