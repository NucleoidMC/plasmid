package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public interface HandSwingListener {
    EventType<HandSwingListener> EVENT = EventType.create(HandSwingListener.class, listeners -> {
        return (player, hand) -> {
            for (HandSwingListener listener : listeners) {
                listener.onSwingHand(player, hand);
            }
        };
    });

    void onSwingHand(ServerPlayerEntity player, Hand hand);
}
