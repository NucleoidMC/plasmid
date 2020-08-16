package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDamageListener {
    EventType<PlayerDamageListener> EVENT = EventType.create(PlayerDamageListener.class, listeners -> (player, source, amount) -> {
        for (PlayerDamageListener listener : listeners) {
            if (listener.onDamage(player, source, amount)) {
                return true;
            }
        }
        return false;
    });

    boolean onDamage(ServerPlayerEntity player, DamageSource source, float amount);
}
