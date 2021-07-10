package xyz.nucleoid.plasmid.game.portal.on_demand;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLifecycle;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

import java.util.concurrent.CompletableFuture;

public final class OnDemandGame {
    private final Identifier gameId;

    private CompletableFuture<ManagedGameSpace> gameFuture;

    public OnDemandGame(Identifier gameId) {
        this.gameId = gameId;
    }

    public Text getName() {
        var config = GameConfigs.get(this.gameId);
        if (config != null) {
            return config.getName().shallowCopy().formatted(Formatting.AQUA);
        } else {
            return new LiteralText(this.gameId.toString()).formatted(Formatting.RED);
        }
    }

    public CompletableFuture<ManagedGameSpace> getOrOpen(MinecraftServer server) {
        if (this.gameFuture == null) {
            this.gameFuture = this.openGame(server);
        }
        return this.gameFuture;
    }

    private void onClose() {
        this.gameFuture = null;
    }

    private CompletableFuture<ManagedGameSpace> openGame(MinecraftServer server) {
        var config = GameConfigs.get(this.gameId);
        if (config == null) {
            var future = new CompletableFuture<ManagedGameSpace>();
            var error = new TranslatableText("text.plasmid.game_config.game_config_does_not_exist", this.gameId);
            future.completeExceptionally(new GameOpenException(error));
            return future;
        }

        return GameSpaceManager.get().open(config).thenApplyAsync(gameSpace -> {
            var lifecycle = gameSpace.getLifecycle();
            lifecycle.addListeners(new LifecycleListeners());

            return gameSpace;
        }, server);
    }

    public int getPlayerCount() {
        var future = this.gameFuture;
        if (future != null) {
            var game = future.getNow(null);
            if (game != null) {
                return game.getPlayerCount();
            }
        }
        return 0;
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
            OnDemandGame.this.onClose();
        }
    }
}
