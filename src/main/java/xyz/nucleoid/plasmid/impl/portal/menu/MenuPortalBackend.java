package xyz.nucleoid.plasmid.impl.portal.menu;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.impl.portal.game.ConcurrentGamePortalBackend;
import xyz.nucleoid.plasmid.api.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MenuPortalBackend implements GamePortalBackend {
    private final Text name;
    private final List<MenuEntry> games;
    private final List<Text> description;
    private final ItemStack icon;

    MenuPortalBackend(Text name, List<Text> description, ItemStack icon, List<MenuPortalConfig.Entry> games) {
        this.name = name;
        this.description = description;
        this.icon = icon;


        this.games = this.buildGames(games);
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
        for (var entry : this.games) {
            entry.provideGameSpaces(consumer);
        }
    }

    private List<GuiElementInterface> getGuiElements() {
        List<GuiElementInterface> elements = new ArrayList<>();

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
                    configEntry.icon().orElse(config.value().icon())
            ));
        }

        return games;
    }

    @Override
    public void applyTo(ServerPlayerEntity player, boolean alt) {
        var ui = Guis.createSelectorGui(player, this.name.copy(), true, this.getGuiElements());
        ui.open();
    }
}
