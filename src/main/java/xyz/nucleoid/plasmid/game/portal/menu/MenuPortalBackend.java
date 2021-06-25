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
        List<GameEntry> games = new ArrayList<>(configs.size());
        for (MenuPortalConfig.Entry configEntry : configs) {
            OnDemandGame game = new OnDemandGame(configEntry.game);
            games.add(new GameEntry(game, configEntry.icon));
        }

        return games;
    }

    @Override
    public void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, this.name);

        int count = 0;
        for (GameEntry entry : this.games) {
            count += entry.game.getPlayerCount();
        }

        display.set(GamePortalDisplay.PLAYER_COUNT, count);
    }

    @Override
    public CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player) {
        CompletableFuture<GameSpace> future = new CompletableFuture<>();

        ShopUi ui = ShopUi.create(this.name, builder -> {
            for (GameEntry entry : this.games) {
                ShopEntry uiEntry = ShopEntry.ofIcon(entry.icon).noCost()
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

        player.openHandledScreen(ui);

        return future;
    }

    static class GameEntry {
        final OnDemandGame game;
        final ItemStack icon;

        GameEntry(OnDemandGame game, ItemStack icon) {
            this.game = game;
            this.icon = icon;
        }
    }
}
