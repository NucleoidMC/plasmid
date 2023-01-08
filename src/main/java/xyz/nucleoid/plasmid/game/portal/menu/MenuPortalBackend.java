package xyz.nucleoid.plasmid.game.portal.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.game.portal.game.ConcurrentGamePortalBackend;
import xyz.nucleoid.plasmid.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MenuPortalBackend implements GamePortalBackend {
    private final Text name;
    private final List<MenuEntry> games;
    private final MutableText hologramName;
    private final List<Text> description;
    private final ItemStack icon;

    MenuPortalBackend(Text name, List<Text> description, ItemStack icon, List<MenuPortalConfig.Entry> games) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        var hologramName = name.copy();

        if (hologramName.getStyle().getColor() == null) {
            hologramName.setStyle(hologramName.getStyle().withColor(Formatting.AQUA));
        }

        this.hologramName = hologramName;
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
        var list = new ObjectOpenCustomHashSet<GameSpace>(Util.identityHashStrategy());
        provideGameSpaces(list::add);
        for (var entry : list) {
            count += Math.max(0, entry.getPlayers().size());
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
            var uiEntry = this.createIconFor(game).build();
            elements.add(uiEntry);
        }

        return elements;
    }

    @Override
    public GuiProvider getGuiProvider() {
        return this::getGuiElements;
    }

    private List<MenuEntry> buildGames(List<MenuPortalConfig.Entry> configs) {
        var games = new ArrayList<MenuEntry>(configs.size());
        for (var configEntry : configs) {
            var game = new ConcurrentGamePortalBackend(configEntry.game());
            var gameConfig = GameConfigs.get(configEntry.game());

            if (gameConfig != null) {
                games.add(new GameMenuEntry(
                        game,
                        configEntry.name().orElse(gameConfig.name()),
                        configEntry.description().orElse(gameConfig.description()),
                        configEntry.icon().orElse(gameConfig.icon())
                ));
            } else {
                games.add(new InvalidMenuEntry(game.getName()));
            }
        }

        return games;
    }

    @Override
    public void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, this.hologramName);

        display.set(GamePortalDisplay.PLAYER_COUNT, this.getPlayerCount());
    }

    @Override
    public void applyTo(ServerPlayerEntity player) {
        var ui = Guis.createSelectorGui(player, this.name.copy(), true, this.getGuiElements());
        ui.open();
    }

    private GuiElementBuilder createIconFor(MenuEntry entry) {
            var element = GuiElementBuilder.from(entry.icon().copy()).hideFlags()
                .setName(entry.name().copy());

        for (var line : entry.description()) {
            var text = line.copy();

            if (line.getStyle().getColor() == null) {
                text.setStyle(line.getStyle().withColor(Formatting.GRAY));
            }

            element.addLoreLine(text);
        }

        var playerCount = entry.getPlayerCount();
        if (playerCount > -1) {
            element.addLoreLine(ScreenTexts.EMPTY);
            element.addLoreLine(Text.empty()
                    .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable("text.plasmid.ui.game_join.players",
                            Text.literal(entry.getPlayerCount() + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
            );
        }
        element.setCallback((a, b, c, gui) -> {
            entry.click(gui.getPlayer());
        });

        return element;
    }
}
