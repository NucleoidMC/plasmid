package xyz.nucleoid.plasmid.impl.menu.entry;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntry;

import java.util.List;
import java.util.function.Consumer;

/**
 * One entry bound to two destinations, chosen by which mouse button was used.
 *
 * @param altAction the hint for the second destination, carried on the entry rather than written into the
 * element, so a theme rebuilding its own elements can still show it
 */
public record HybridEntry(
        GameMenuEntry main,
        GameMenuEntry alt,
        Action altAction,
        Component name,
        List<Component> description,
        ItemStack icon
) implements GameMenuEntry {
    @Override
    public void click(ServerPlayer player, boolean alt) {
        if (alt) {
            this.alt.click(player, false);
        } else {
            this.main.click(player, false);
        }
    }

    @Override
    public boolean isGameEntry() {
        return this.main.isGameEntry() || this.alt.isGameEntry();
    }

    /**
     * Both sides, so a category counts everything reachable; duplicates collapse in the caller's set.
     */
    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        this.main.provideGameSpaces(consumer);
        this.alt.provideGameSpaces(consumer);
    }

    @Override
    public int getPlayerCount() {
        var games = new it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet<GameSpace>();
        this.provideGameSpaces(games::add);

        int count = 0;
        for (var gameSpace : games) {
            count += gameSpace.getState().players();
        }
        return count;
    }

    @Override
    public Action getAction() {
        return this.main.getAction();
    }

    @Override
    public Action getAltAction() {
        return this.altAction;
    }

    @Override
    public GuiElement createGuiElement() {
        // Only the binding differs from the default: the second destination is on right-click, where an
        // ordinary entry reads shift. The alt hint itself comes from getAltAction, so a theme that draws its
        // own elements shows it too.
        return GuiElementBuilder.from(GameMenuEntry.super.createGuiElement().getItemStack().copy())
                .setCallback((index, type, action, gui) -> this.click(gui.getPlayer(), type.isRight))
                .build();
    }
}
