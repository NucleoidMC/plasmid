package xyz.nucleoid.plasmid.game.player.isolation;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

public interface PlayerManagerAccess {
    void plasmid$savePlayerData(ServerPlayerEntity player);
    void plasmid$AddPlayerAndSendDefaultJoinPacket(ServerPlayerEntity player, PlayerSet watchers, boolean firstSpawn);
    boolean plasmid$playerInstanceAlreadyExists(ServerPlayerEntity player);
    void plasmid$removePlayer(ServerPlayerEntity player, PlayerSet watchers);
    void plasmid$sendToAllFrom(Packet<?> packet, ServerPlayerEntity player);
}
