package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDeathListener {
    EventType<PlayerDeathListener> EVENT = EventType.create(PlayerDeathListener.class, listeners -> {
        return (game, player, source) -> {
            for (PlayerDeathListener listener : listeners) {
                if (listener.onDeath(game, player, source)) {
                    return true;
                }
            }
            return false;
        };
    });

    boolean onDeath(Game game, ServerPlayerEntity player, DamageSource source);
}
