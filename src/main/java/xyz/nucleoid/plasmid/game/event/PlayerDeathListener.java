package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDeathListener {
    EventType<PlayerDeathListener> EVENT = EventType.create(PlayerDeathListener.class, listeners -> {
        return (player, source) -> {
            for (PlayerDeathListener listener : listeners) {
                if (listener.onDeath(player, source)) {
                    return true;
                }
            }
            return false;
        };
    });

    boolean onDeath(ServerPlayerEntity player, DamageSource source);
}
