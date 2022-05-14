package xyz.nucleoid.plasmid.game.portal.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;
import xyz.nucleoid.plasmid.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MenuPortalBackend implements GamePortalBackend {
    private final Text name;
    private final List<Text> description;
    private final ItemStack icon;
    private final List<MenuEntry> entries;
    private final MutableText hologramName;

    MenuPortalBackend(Text name, List<Text> description, ItemStack icon, List<MenuEntryConfig> entries) {
        this.name = name;
        var hologramName = name.shallowCopy();

        if (hologramName.getStyle().getColor() == null) {
            hologramName.setStyle(hologramName.getStyle().withColor(Formatting.AQUA));
        }

        this.hologramName = hologramName;
        this.description = description;
        this.icon = icon;
        this.entries = this.buildEntries(entries);
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public List<Text> getDescription() {
        return this.description;
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public int getPlayerCount() {
        int count = 0;
        for (var entry : this.entries) {
            count += entry.getPlayerCount();
        }
        return count;
    }

    private List<GuiElementInterface> getGuiElements(CompletableFuture<GameSpace> future) {
        List<GuiElementInterface> elements = new ArrayList<>();

        for (var entry : this.entries) {
            var uiEntry = this.createIconFor(entry, future).build();
            elements.add(uiEntry);
        }

        return elements;
    }

    @Override
    public GuiProvider getGuiProvider() {
        return this::getGuiElements;
    }

    private List<MenuEntry> buildEntries(List<MenuEntryConfig> configs) {
        var entries = new ArrayList<MenuEntry>(configs.size());
        for (var configEntry : configs) {
            entries.add(configEntry.createEntry());
        }

        return entries;
    }

    @Override
    public void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, this.hologramName);

        display.set(GamePortalDisplay.PLAYER_COUNT, this.getPlayerCount());
    }

    @Override
    public CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player) {
        var future = new CompletableFuture<GameSpace>();

        var ui = Guis.createSelectorGui(player, this.name.shallowCopy(), true, this.getGuiElements(future));
        ui.open();

        return future;
    }

    private GuiElementBuilder createIconFor(MenuEntry entry, CompletableFuture<GameSpace> future) {
            var element = GuiElementBuilder.from(entry.icon().copy())
                .setName(entry.name().shallowCopy());

        for (var line : entry.description()) {
            var text = line.shallowCopy();

            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withColor(Formatting.GRAY));
            }

            element.addLoreLine(text);
        }

        
        element.addLoreLine(LiteralText.EMPTY);
        element.addLoreLine(new LiteralText("")
                .append(new LiteralText("» ").formatted(Formatting.DARK_GRAY))
                .append(new TranslatableText("text.plasmid.ui.game_join.players",
                        new LiteralText(entry.getPlayerCount() + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
        );

        element.setCallback((a, b, c, gui) -> {
            entry.click(gui.getPlayer(), future);
        });

        return element;
    }
}
