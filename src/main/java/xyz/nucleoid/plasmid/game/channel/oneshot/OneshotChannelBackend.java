package xyz.nucleoid.plasmid.game.channel.oneshot;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameLifecycle;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.channel.GameChannelBackend;
import xyz.nucleoid.plasmid.game.channel.GameChannelMembers;
import xyz.nucleoid.plasmid.game.player.JoinResult;

import java.util.concurrent.CompletableFuture;

public final class OneshotChannelBackend implements GameChannelBackend {
    private final ManagedGameSpace gameSpace;
    private final GameChannelMembers members;

    public OneshotChannelBackend(ManagedGameSpace gameSpace, GameChannelMembers members) {
        this.gameSpace = gameSpace;
        this.members = members;

        this.gameSpace.getLifecycle().addListeners(new LifecycleListeners());
    }

    @Override
    public Text getName() {
        return new LiteralText(this.gameSpace.getGameConfig().getName()).formatted(Formatting.AQUA);
    }

    @Override
    public CompletableFuture<JoinResult> requestJoin(ServerPlayerEntity player) {
        return this.gameSpace.offerPlayer(player);
    }

    private class LifecycleListeners implements GameLifecycle.Listeners {
        @Override
        public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            OneshotChannelBackend.this.members.addPlayer(player);
        }

        @Override
        public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
            OneshotChannelBackend.this.members.removePlayer(player);
        }
    }
}
