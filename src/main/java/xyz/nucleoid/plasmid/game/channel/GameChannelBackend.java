package xyz.nucleoid.plasmid.game.channel;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.player.JoinResult;

import java.util.concurrent.CompletableFuture;

public interface GameChannelBackend {
    Text getName();

    CompletableFuture<JoinResult> requestJoin(ServerPlayerEntity player);
}
