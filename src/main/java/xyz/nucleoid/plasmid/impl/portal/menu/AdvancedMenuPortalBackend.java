package xyz.nucleoid.plasmid.impl.portal.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.api.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AdvancedMenuPortalBackend implements GamePortalBackend {
    private final Component name;

    private final List<Component> description;
    private final ItemStack icon;

    private final List<MenuEntryConfig> entryConfigs;
    private List<MenuEntry> entries;

    AdvancedMenuPortalBackend(Component name, List<Component> description, ItemStack icon, List<MenuEntryConfig> entryConfigs) {
        this.name = name;

        this.description = description;
        this.icon = icon;

        this.entryConfigs = entryConfigs;
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public List<Component> getDescription() {
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

    private List<GuiElement> getGuiElements() {
        List<GuiElement> elements = new ArrayList<>();

        for (var entry : this.getEntries()) {
            var uiEntry = entry.createGuiElement();
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
    public void applyTo(ServerPlayer player, boolean alt) {
        var ui = Guis.createSelectorGui(player, this.name.copy(), true, this.getGuiElements());
        ui.open();
    }
}
