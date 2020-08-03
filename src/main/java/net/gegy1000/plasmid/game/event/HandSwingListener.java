package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public interface HandSwingListener {
    EventType<HandSwingListener> EVENT = EventType.create(HandSwingListener.class, listeners -> {
        return (game, player, hand) -> {
            for (HandSwingListener listener : listeners) {
                listener.onSwingHand(game, player, hand);
            }
        };
    });

    void onSwingHand(Game game, ServerPlayerEntity player, Hand hand);
}
