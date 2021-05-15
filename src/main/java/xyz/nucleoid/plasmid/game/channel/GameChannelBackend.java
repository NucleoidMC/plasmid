package xyz.nucleoid.plasmid.game.channel;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public interface GameChannelBackend {
    Text getName();

    CompletableFuture<ChannelJoinTicket> requestJoin(ServerPlayerEntity player);

    interface Factory {
        GameChannelBackend create(MinecraftServer server, Identifier id, GameChannelMembers members);
    }
}
