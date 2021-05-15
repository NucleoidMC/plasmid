package xyz.nucleoid.plasmid.game.channel.menu;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameLifecycle;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.channel.ChannelJoinTicket;
import xyz.nucleoid.plasmid.game.channel.GameChannelBackend;
import xyz.nucleoid.plasmid.game.channel.GameChannelMembers;
import xyz.nucleoid.plasmid.game.channel.on_demand.OnDemandGame;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MenuChannelBackend implements GameChannelBackend {
    private final Text name;
    private final List<GameEntry> games;
    private final GameChannelMembers members;

    MenuChannelBackend(Text name, List<MenuChannelConfig.Entry> games, GameChannelMembers members) {
        this.name = name;
        this.games = this.buildGames(games);
        this.members = members;
    }

    private List<GameEntry> buildGames(List<MenuChannelConfig.Entry> configs) {
        LifecycleListeners lifecycleListeners = new LifecycleListeners();

        List<GameEntry> games = new ArrayList<>(configs.size());
        for (MenuChannelConfig.Entry configEntry : configs) {
            OnDemandGame game = new OnDemandGame(configEntry.game);
            game.setLifecycleListeners(lifecycleListeners);

            games.add(new GameEntry(game, configEntry.icon));
        }

        return games;
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public CompletableFuture<ChannelJoinTicket> requestJoin(ServerPlayerEntity player) {
        CompletableFuture<ChannelJoinTicket> future = new CompletableFuture<>();

        ShopUi ui = ShopUi.create(this.name, builder -> {
            for (GameEntry entry : this.games) {
                ShopEntry uiEntry = ShopEntry.ofIcon(entry.icon).noCost()
                        .withName(entry.game.getName())
                        .onBuy(p -> {
                            entry.game.getOrOpen(player.server).handle((gameSpace, throwable) -> {
                                if (throwable == null) {
                                    future.complete(ChannelJoinTicket.forGameSpace(gameSpace));
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

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            MenuChannelBackend.this.members.addPlayer(player);
        }

        @Override
        public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            MenuChannelBackend.this.members.removePlayer(player);
        }
    }
}
