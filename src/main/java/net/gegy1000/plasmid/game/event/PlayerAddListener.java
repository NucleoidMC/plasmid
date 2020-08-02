package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerAddListener {
    EventType<PlayerAddListener> EVENT = EventType.create(PlayerAddListener.class, listeners -> {
        return (game, player) -> {
            for (PlayerAddListener listener : listeners) {
                listener.onAddPlayer(game, player);
            }
        };
    });

    void onAddPlayer(Game game, ServerPlayerEntity player);
}
