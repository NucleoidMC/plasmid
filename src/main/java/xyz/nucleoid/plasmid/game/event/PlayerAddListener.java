package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerAddListener {
    EventType<PlayerAddListener> EVENT = EventType.create(PlayerAddListener.class, listeners -> {
        return player -> {
            for (PlayerAddListener listener : listeners) {
                listener.onAddPlayer(player);
            }
        };
    });

    void onAddPlayer(ServerPlayerEntity player);
}
