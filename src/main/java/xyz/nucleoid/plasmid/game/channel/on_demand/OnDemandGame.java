package xyz.nucleoid.plasmid.game.channel.on_demand;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

import java.util.concurrent.CompletableFuture;

final class OnDemandGame {
    private final Identifier gameId;

    private GameLifecycle.Listeners lifecycleListeners;
    private CompletableFuture<ManagedGameSpace> gameFuture;

    OnDemandGame(Identifier gameId) {
        this.gameId = gameId;
    }

    public Text getName() {
        ConfiguredGame<?> configuredGame = GameConfigs.get(this.gameId);
        if (configuredGame != null) {
            return new LiteralText(configuredGame.getDisplayName(this.gameId)).formatted(Formatting.AQUA);
        } else {
            return new LiteralText(this.gameId.toString()).formatted(Formatting.RED);
        }
    }

    public void setLifecycleListeners(GameLifecycle.Listeners lifecycleListeners) {
        this.lifecycleListeners = lifecycleListeners;
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
        ConfiguredGame<?> config = GameConfigs.get(this.gameId);
        if (config == null) {
            CompletableFuture<ManagedGameSpace> future = new CompletableFuture<>();
            TranslatableText error = new TranslatableText("Game config with id '%s' does not exist!", this.gameId);
            future.completeExceptionally(new GameOpenException(error));
            return future;
        }

        // TODO: Hook here for game opening event
        GameEvents.OPENING.invoker().onGameOpening(this.gameId, config, false);

        return config.open(server).thenApplyAsync(gameSpace -> {
            GameLifecycle lifecycle = gameSpace.getLifecycle();
            lifecycle.addListeners(new LifecycleListeners());
            if (this.lifecycleListeners != null) {
                lifecycle.addListeners(this.lifecycleListeners);
            }

            // TODO: Hook here for game opened event
            GameEvents.OPEN.invoker().onGameOpen(this.gameId, config, false, gameSpace);

            return gameSpace;
        }, server);
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
            OnDemandGame.this.onClose();
        }
    }
}
