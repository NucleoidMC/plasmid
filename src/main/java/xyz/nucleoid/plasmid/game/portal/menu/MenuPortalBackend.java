package xyz.nucleoid.plasmid.game.portal.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.game.portal.on_demand.OnDemandGame;
import xyz.nucleoid.plasmid.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MenuPortalBackend implements GamePortalBackend {
    private final Text name;
    private final List<GameEntry> games;

    MenuPortalBackend(Text name, List<MenuPortalConfig.Entry> games) {
        this.name = name.shallowCopy().formatted(Formatting.AQUA);
        this.games = this.buildGames(games);
    }

    private List<GameEntry> buildGames(List<MenuPortalConfig.Entry> configs) {
        var games = new ArrayList<GameEntry>(configs.size());
        for (var configEntry : configs) {
            var game = new OnDemandGame(configEntry.game());
            games.add(new GameEntry(game, configEntry.icon()));
        }

        return games;
    }

    @Override
    public void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, this.name);

        int count = 0;
        for (var entry : this.games) {
            count += entry.game.getPlayerCount();
        }

        display.set(GamePortalDisplay.PLAYER_COUNT, count);
    }

    @Override
    public CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player) {
        var future = new CompletableFuture<GameSpace>();

        List<GuiElementInterface> games = new ArrayList<>();

        for (var entry : this.games) {
            var uiEntry = GuiElementBuilder.from(entry.icon)
                    .setName(entry.game.getName().shallowCopy())
                    .setCallback((x, y, z) -> entry.game.getOrOpen(player.server).handle((gameSpace, throwable) -> {
                        if (throwable == null) {
                            future.complete(gameSpace);
                        } else {
                            future.completeExceptionally(throwable);
                        }
                        return null;
                    }))
                    .build();

            games.add(uiEntry);
        }

        var ui = Guis.createSelectorGui(player, this.name.shallowCopy(), games);
        ui.open();

        return future;
    }

    record GameEntry(OnDemandGame game, ItemStack icon) {
    }
}
