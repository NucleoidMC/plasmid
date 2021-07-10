package xyz.nucleoid.plasmid.game.player.isolation;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerManagerAccess {
    void plasmid$savePlayerData(ServerPlayerEntity player);

    void plasmid$loadIntoPlayer(ServerPlayerEntity player);

    PlayerResetter plasmid$getPlayerResetter();
}
