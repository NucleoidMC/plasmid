package xyz.nucleoid.plasmid.game.channel.on_demand;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLifecycle;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.channel.GameChannelBackend;
import xyz.nucleoid.plasmid.game.channel.GameChannelMembers;
import xyz.nucleoid.plasmid.game.player.JoinResult;

import java.util.concurrent.CompletableFuture;

public final class OnDemandChannelBackend implements GameChannelBackend {
    private final OnDemandGame game;

    private final GameChannelMembers members;

    public OnDemandChannelBackend(Identifier gameId, GameChannelMembers members) {
        this.game = new OnDemandGame(gameId);
        this.game.setLifecycleListeners(new LifecycleListeners());

        this.members = members;
    }

    @Override
    public Text getName() {
        return this.game.getName();
    }

    @Override
    public CompletableFuture<JoinResult> requestJoin(ServerPlayerEntity player) {
        return this.game.getOrOpen(player.server)
                .thenCompose(gameSpace -> gameSpace.offerPlayer(player));
    }

    private void onGameClose() {
        this.members.clear();
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            OnDemandChannelBackend.this.members.addPlayer(player);
        }

        @Override
        public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            OnDemandChannelBackend.this.members.removePlayer(player);
        }

        @Override
        public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
            OnDemandChannelBackend.this.onGameClose();
        }
    }
}
