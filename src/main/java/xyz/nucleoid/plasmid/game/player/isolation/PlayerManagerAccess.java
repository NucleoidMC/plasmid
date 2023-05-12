package xyz.nucleoid.plasmid.game.player.isolation;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerManagerAccess {
    void plasmid$savePlayerData(ServerPlayerEntity player);
    void plasmid$AddPlayerAndSendDefaultJoinPacket(ServerPlayerEntity player, boolean firstSpawn);
    boolean plasmid$playerInstanceAlreadyExists(ServerPlayerEntity player);
    void plasmid$removePlayer(ServerPlayerEntity player);
}
