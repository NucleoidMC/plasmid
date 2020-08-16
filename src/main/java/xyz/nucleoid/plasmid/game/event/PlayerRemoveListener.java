package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerRemoveListener {
    EventType<PlayerRemoveListener> EVENT = EventType.create(PlayerRemoveListener.class, listeners -> player -> {
        for (PlayerRemoveListener listener : listeners) {
            listener.onRemovePlayer(player);
        }
    });

    void onRemovePlayer(ServerPlayerEntity player);
}
