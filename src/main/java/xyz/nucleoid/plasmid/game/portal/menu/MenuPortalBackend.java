package xyz.nucleoid.plasmid.game.portal.menu;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;
import xyz.nucleoid.plasmid.game.portal.on_demand.OnDemandGame;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

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

        var ui = ShopUi.create(player, this.name, builder -> {
            for (var entry : this.games) {
                var uiEntry = ShopEntry.ofIcon(entry.icon).noCost()
                        .withName(entry.game.getName())
                        .onBuy(p -> {
                            entry.game.getOrOpen(player.server).handle((gameSpace, throwable) -> {
                                if (throwable == null) {
                                    future.complete(gameSpace);
                                } else {
                                    future.completeExceptionally(throwable);
                                }
                                return null;
                            });
                        });

                builder.add(uiEntry);
            }
        });

        ui.open();

        return future;
    }

    record GameEntry(OnDemandGame game, ItemStack icon) {
    }
}
