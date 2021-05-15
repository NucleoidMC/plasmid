package xyz.nucleoid.plasmid.game.channel;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.player.JoinResult;

import java.util.concurrent.CompletableFuture;

public interface ChannelJoinTicket {
    static ChannelJoinTicket forGameSpace(ManagedGameSpace gameSpace) {
        return gameSpace::offerPlayer;
    }

    CompletableFuture<JoinResult> tryJoin(ServerPlayerEntity player);
}
