package xyz.nucleoid.plasmid.game.portal.on_demand;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.config.GameConfigLists;

import java.util.concurrent.CompletableFuture;

public final class OnDemandGame {
    private final Identifier gameId;

    private CompletableFuture<GameSpace> gameFuture;

    public OnDemandGame(Identifier gameId) {
        this.gameId = gameId;
    }

    public Text getName() {
        var config = GameConfigLists.composite().byKey(this.gameId);
        if (config != null) {
            return config.name().copy().formatted(Formatting.AQUA);
        } else {
            return Text.literal(this.gameId.toString()).formatted(Formatting.RED);
        }
    }

    public CompletableFuture<GameSpace> getOrOpen(MinecraftServer server) {
        var future = this.gameFuture;
        if (future == null || this.isInvalid(future)) {
            this.gameFuture = future = this.openGame(server);
        }
        return future;
    }

    private boolean isInvalid(CompletableFuture<GameSpace> gameFuture) {
        if (gameFuture.isCompletedExceptionally()) {
            return true;
        }
        return gameFuture.isDone() && gameFuture.join().isClosed();
    }

    private CompletableFuture<GameSpace> openGame(MinecraftServer server) {
        var config = GameConfigLists.composite().byKey(this.gameId);
        if (config == null) {
            Plasmid.LOGGER.warn("Missing game config for on-demand game with id '{}'", this.gameId);

            var future = new CompletableFuture<GameSpace>();
            var error = Text.translatable("text.plasmid.game_config.game_config_does_not_exist", this.gameId);
            future.completeExceptionally(new GameOpenException(error));

            return future;
        }

        return config.open(server);
    }

    public int getPlayerCount() {
        var future = this.gameFuture;
        if (future != null && future.isDone() && !this.isInvalid(future)) {
            var game = future.join();
            return game.getPlayers().size();
        }
        return 0;
    }
}
