package xyz.nucleoid.plasmid.impl.player.isolation;

import net.minecraft.server.level.ServerPlayer;

public interface PlayerManagerAccess {
    void plasmid$savePlayerData(ServerPlayer player);

    void plasmid$loadIntoPlayer(ServerPlayer player);

    PlayerResetter plasmid$getPlayerResetter();
}
