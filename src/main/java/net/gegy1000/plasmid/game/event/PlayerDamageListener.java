package net.gegy1000.plasmid.game.event;

import net.gegy1000.plasmid.game.Game;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDamageListener {
    EventType<PlayerDamageListener> EVENT = EventType.create(PlayerDamageListener.class, listeners -> {
        return (game, player, source, amount) -> {
            for (PlayerDamageListener listener : listeners) {
                if (listener.onDamage(game, player, source, amount)) {
                    return true;
                }
            }
            return false;
        };
    });

    boolean onDamage(Game game, ServerPlayerEntity player, DamageSource source, float amount);
}
