package xyz.nucleoid.plasmid.game.portal.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.portal.GamePortal.GuiProvider;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.game.portal.on_demand.OnDemandGame;
import xyz.nucleoid.plasmid.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MenuPortalBackend implements GamePortalBackend {
    private final Text name;
    private final List<MenuEntry> games;
    private final MutableText hologramName;

    MenuPortalBackend(Text name, List<MenuPortalConfig.Entry> games) {
        this.name = name;
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
    public int getPlayerCount() {
        int count = 0;
        for (var entry : this.games) {
            count += entry.getPlayerCount();
        }
        return count;
    }

    private List<GuiElementInterface> getGuiElements(CompletableFuture<GameSpace> future) {
        List<GuiElementInterface> elements = new ArrayList<>();

        for (var game : this.games) {
            var uiEntry = this.createIconFor(game, future).build();
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
            var game = new OnDemandGame(configEntry.game());
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
    public CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player) {
        var future = new CompletableFuture<GameSpace>();

        var ui = Guis.createSelectorGui(player, this.name.copy(), true, this.getGuiElements(future));
        ui.open();

        return future;
    }

    private GuiElementBuilder createIconFor(MenuEntry entry, CompletableFuture<GameSpace> future) {
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
