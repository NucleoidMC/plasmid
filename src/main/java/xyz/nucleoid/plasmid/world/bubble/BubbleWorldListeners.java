package xyz.nucleoid.plasmid.world.bubble;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public final class BubbleWorldListeners {
    Consumer<ServerPlayerEntity> removePlayer = player -> {};

    public void onRemovePlayer(Consumer<ServerPlayerEntity> listener) {
        this.removePlayer = listener;
    }
}
