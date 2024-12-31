package xyz.nucleoid.plasmid.impl.portal.backend.menu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.backend.PortalUserContext;
import xyz.nucleoid.plasmid.impl.portal.backend.GamePortalBackend;
import xyz.nucleoid.plasmid.api.util.Guis;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuEntry;
import xyz.nucleoid.plasmid.impl.portal.menu.MenuEntryConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AdvancedMenuPortalBackend implements GamePortalBackend {
    private final Text name;

    private final List<Text> description;
    private final ItemStack icon;

    private final List<MenuEntryConfig> entryConfigs;
    private List<MenuEntry> entries;

    public AdvancedMenuPortalBackend(Text name, List<Text> description, ItemStack icon, List<MenuEntryConfig> entryConfigs) {
        this.name = name;

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
        var uniqueGameSpaces = new ReferenceOpenHashSet<GameSpace>();
        provideGameSpaces(uniqueGameSpaces::add);
        for (var entry : uniqueGameSpaces) {
            count += Math.max(0, entry.getState().players());
        }
        return count;
    }

    @Override
    public int getSpectatorCount() {
        int count = 0;
        var uniqueGameSpaces = new ReferenceOpenHashSet<GameSpace>();
        provideGameSpaces(uniqueGameSpaces::add);
        for (var entry : uniqueGameSpaces) {
            count += Math.max(0, entry.getState().spectators());
        }
        return count;
    }

    @Override
    public void provideGameSpaces(Consumer<GameSpace> consumer) {
        for (var entry : this.getEntries()) {
            entry.provideGameSpaces(consumer);
        }
    }

    private List<GuiElementInterface> getGuiElements(PortalUserContext context) {
        List<GuiElementInterface> elements = new ArrayList<>();

        for (var entry : this.getEntries()) {
            var uiEntry = entry.createGuiElement(context);
            elements.add(uiEntry);
        }

        return elements;
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
    public void applyTo(PortalUserContext context, ClickType type) {
        context.openUi(player -> Guis.createSelectorGui(player, this.name.copy(), true, this.getGuiElements(context)).open());
    }
}