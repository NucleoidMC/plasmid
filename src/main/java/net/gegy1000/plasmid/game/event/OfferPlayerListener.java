package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.JoinResult;
import net.minecraft.server.network.ServerPlayerEntity;

public interface OfferPlayerListener {
    EventType<OfferPlayerListener> EVENT = EventType.create(OfferPlayerListener.class, listeners -> {
        return (game, player) -> {
            for (OfferPlayerListener listener : listeners) {
                JoinResult result = listener.offerPlayer(game, player);
                if (result.isErr()) {
                    return result;
                }
            }
            return JoinResult.ok();
        };
    });

    JoinResult offerPlayer(Game game, ServerPlayerEntity player);
}
