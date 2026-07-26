package xyz.nucleoid.plasmid.impl.portal.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.item.ItemStackTemplate;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.game.ConcurrentGamePortalBackend;
import xyz.nucleoid.plasmid.api.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class MenuPortalBackend implements GamePortalBackend {
    private final Component name;
    private final List<MenuEntry> games;
    private final List<Component> description;
    private final ItemStack icon;

    MenuPortalBackend(Component name, List<Component> description, ItemStack icon, List<MenuPortalConfig.Entry> games) {
        this.name = name;
        this.description = description;
        this.icon = icon;


        this.games = this.buildGames(games);
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
        for (var entry : this.games) {
            entry.provideGameSpaces(consumer);
        }
    }

    private List<GuiElement> getGuiElements() {
        List<GuiElement> elements = new ArrayList<>();

        for (var game : this.games) {
            var uiEntry = game.createGuiElement();
            elements.add(uiEntry);
        }

        return elements;
    }

    private List<MenuEntry> buildGames(List<MenuPortalConfig.Entry> configs) {
        var games = new ArrayList<MenuEntry>(configs.size());
        for (var configEntry : configs) {
            var config = configEntry.game();
            var game = new ConcurrentGamePortalBackend(config);
            games.add(new GameMenuEntry(
                    game,
                    configEntry.name().orElse(GameConfig.name(config)),
                    configEntry.description().orElse(config.value().description()),
                    configEntry.icon().map(ItemStackTemplate::create).orElse(config.value().icon())
            ));
        }

        return games;
    }

    @Override
    public void applyTo(ServerPlayer player, boolean alt) {
        var ui = Guis.createSelectorGui(player, this.name.copy(), true, this.getGuiElements());
        ui.open();
    }
}
