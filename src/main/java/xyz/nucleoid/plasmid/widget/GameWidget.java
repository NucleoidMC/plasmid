package xyz.nucleoid.plasmid.widget;

import net.minecraft.server.network.ServerPlayerEntity;

public interface GameWidget extends AutoCloseable {
    void addPlayer(ServerPlayerEntity player);

    void removePlayer(ServerPlayerEntity player);

    @Override
    void close();
}
