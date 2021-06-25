package xyz.nucleoid.plasmid.game.common.widget;

import net.minecraft.server.network.ServerPlayerEntity;

public interface GameWidget extends AutoCloseable {
    void addPlayer(ServerPlayerEntity player);

    void removePlayer(ServerPlayerEntity player);

    @Override
    void close();
}
