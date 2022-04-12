package xyz.nucleoid.plasmid.game.portal.on_demand;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.portal.GamePortalBackend;
import xyz.nucleoid.plasmid.game.portal.GamePortalDisplay;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class OnDemandPortalBackend implements GamePortalBackend {
    private final OnDemandGame game;

    public OnDemandPortalBackend(Identifier gameId) {
        this.game = new OnDemandGame(gameId);
    }

    @Override
    public void populateDisplay(GamePortalDisplay display) {
        display.set(GamePortalDisplay.NAME, this.game.getName());
        display.set(GamePortalDisplay.PLAYER_COUNT, this.game.getPlayerCount());
    }

    @Override
    public Text getName() {
        return this.game.getName();
    }

    @Override
    public int getPlayerCount() {
        return this.game.getPlayerCount();
    }

    @Override
    public CompletableFuture<GameSpace> requestJoin(ServerPlayerEntity player) {
        return this.game.getOrOpen(player.server).thenApply(Function.identity());
    }
}
