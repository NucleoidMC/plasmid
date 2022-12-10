package xyz.nucleoid.plasmid.game.portal.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.ListedGameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AdvancedMenuPortalBackend implements GamePortalBackend {
    private final Text name;
    private final MutableText hologramName;

    private final List<Text> description;
    private final ItemStack icon;

    private final List<MenuEntryConfig> entryConfigs;
    private List<MenuEntry> entries;

    AdvancedMenuPortalBackend(Text name, List<Text> description, ItemStack icon, List<MenuEntryConfig> entryConfigs) {
        this.name = name;
        var hologramName = name.copy();

        if (hologramName.getStyle().getColor() == null) {
            hologramName.setStyle(hologramName.getStyle().withColor(Formatting.AQUA));
        }

        this.hologramName = hologramName;
        this.description = description;
        this.icon = icon;

        this.entryConfigs = entryConfigs;
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
        for (var entry : this.getEntries()) {
            count += entry.getPlayerCount();
        }
        return count;
    }

    private List<GuiElementInterface> getGuiElements(CompletableFuture<ListedGameSpace> future) {
        List<GuiElementInterface> elements = new ArrayList<>();

        for (var entry : this.getEntries()) {
            var uiEntry = this.createIconFor(entry, future).build();
            elements.add(uiEntry);
        }

        return elements;
    }

    @Override
    public GuiProvider getGuiProvider() {
        return this::getGuiElements;
    }

    private List<MenuEntry> getEntries() {
        if (this.entries == null) {
            this.entries = new ArrayList<MenuEntry>(this.entryConfigs.size());
            for (var configEntry : this.entryConfigs) {
                this.entries.add(configEntry.createEntry());
            }
        }

        return this.entries;
    }

    @Override
    public void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, this.hologramName);

        display.set(GamePortalDisplay.PLAYER_COUNT, this.getPlayerCount());
    }

    @Override
    public CompletableFuture<ListedGameSpace> requestJoin(ServerPlayerEntity player) {
        var future = new CompletableFuture<ListedGameSpace>();

        var ui = Guis.createSelectorGui(player, this.name.copy(), true, this.getGuiElements(future));
        ui.open();

        return future;
    }

    private GuiElementBuilder createIconFor(MenuEntry entry, CompletableFuture<ListedGameSpace> future) {
            var element = GuiElementBuilder.from(entry.icon().copy())
                .setName(entry.name().copy());

        for (var line : entry.description()) {
            var text = line.copy();

            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withColor(Formatting.GRAY));
            }

            element.addLoreLine(text);
        }

        
        element.addLoreLine(ScreenTexts.EMPTY);
        element.addLoreLine(Text.empty()
                .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                .append(Text.translatable("text.plasmid.ui.game_join.players",
                        Text.literal(entry.getPlayerCount() + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
        );

        element.setCallback((a, b, c, gui) -> {
            entry.click(gui.getPlayer(), future);
        });

        return element;
    }
}
