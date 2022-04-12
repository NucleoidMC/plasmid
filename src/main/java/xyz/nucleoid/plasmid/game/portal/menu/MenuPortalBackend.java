package xyz.nucleoid.plasmid.game.portal.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.game.portal.on_demand.OnDemandGame;
import xyz.nucleoid.plasmid.util.Guis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MenuPortalBackend implements GamePortalBackend {
    private final Text name;
    private final List<GameEntry> games;
    private final MutableText hologramName;

    MenuPortalBackend(Text name, List<MenuPortalConfig.Entry> games) {
        this.name = name;
        var hologramName = name.shallowCopy();

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
            count += entry.game.getPlayerCount();
        }
        return count;
    }

    private List<GameEntry> buildGames(List<MenuPortalConfig.Entry> configs) {
        var games = new ArrayList<GameEntry>(configs.size());
        for (var configEntry : configs) {
            var game = new OnDemandGame(configEntry.game());
            var gameConfig = GameConfigs.get(configEntry.game());

            if (gameConfig != null) {
                games.add(new GameEntry(
                        game,
                        configEntry.name().orElse(gameConfig.name()),
                        configEntry.description().orElse(gameConfig.description()),
                        configEntry.icon().orElse(gameConfig.icon())
                ));
            } else {
                games.add(new GameEntry(
                        game,
                        game.getName(),
                        Collections.singletonList(new TranslatableText("text.plasmid.game.not_found")),
                        Items.BARRIER.getDefaultStack()
                ));
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

        List<GuiElementInterface> games = new ArrayList<>();

        for (var entry : this.games) {
            var uiEntry = this.createIconFor(entry, future).build();
            games.add(uiEntry);
        }

        var ui = Guis.createSelectorGui(player, this.name.shallowCopy(), true, games);
        ui.open();

        return future;
    }

    private GuiElementBuilder createIconFor(GameEntry entry, CompletableFuture<GameSpace> future) {
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
                        new LiteralText(entry.game.getPlayerCount() + "").formatted(Formatting.YELLOW)).formatted(Formatting.GOLD))
        );

        element.setCallback((a, b, c, gui) -> entry.game.getOrOpen(gui.getPlayer().getServer()).handle((gameSpace, throwable) -> {
            if (throwable == null) {
                future.complete(gameSpace);
            } else {
                future.completeExceptionally(throwable);
            }
            return null;
        }));

        return element;
    }

    record GameEntry(OnDemandGame game, Text name, List<Text> description, ItemStack icon) {
    }
}
