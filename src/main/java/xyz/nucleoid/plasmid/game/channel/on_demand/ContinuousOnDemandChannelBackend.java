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

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ContinuousOnDemandChannelBackend implements GameChannelBackend {
    private final OnDemandGame game;
    private final GameChannelMembers members;

    public ContinuousOnDemandChannelBackend(Identifier gameId, GameChannelMembers members) {
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

    private class LifecycleListeners implements GameLifecycle.Listeners {
        private boolean transfer;

        @Override
        public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            ContinuousOnDemandChannelBackend.this.members.addPlayer(player);
        }

        @Override
        public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            if (this.transfer) {
                ContinuousOnDemandChannelBackend.this.requestJoin(player);
            } else {
                ContinuousOnDemandChannelBackend.this.members.removePlayer(player);
            }
        }

        @Override
        public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
            if (reason == GameCloseReason.FINISHED) {
                this.transfer = true;
            }
        }

        @Override
        public void onClosed(GameSpace gameSpace, List<ServerPlayerEntity> players, GameCloseReason reason) {
            this.transfer = false;
        }
    }
}
