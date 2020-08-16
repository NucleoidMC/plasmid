package xyz.nucleoid.plasmid.game.event;

import xyz.nucleoid.plasmid.game.player.JoinResult;
import net.minecraft.server.network.ServerPlayerEntity;

public interface OfferPlayerListener {
    EventType<OfferPlayerListener> EVENT = EventType.create(OfferPlayerListener.class, listeners -> {
        return player -> {
            for (OfferPlayerListener listener : listeners) {
                JoinResult result = listener.offerPlayer(player);
                if (result.isError()) {
                    return result;
                }
            }
            return JoinResult.ok();
        };
    });

    JoinResult offerPlayer(ServerPlayerEntity player);
}
