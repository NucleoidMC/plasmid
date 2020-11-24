package xyz.nucleoid.plasmid.game.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerDamageListener {
    EventType<PlayerDamageListener> EVENT = EventType.create(PlayerDamageListener.class, listeners -> {
        return (player, source, amount) -> {
            for (PlayerDamageListener listener : listeners) {
                ActionResult result = listener.onDamage(player, source, amount);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        };
    });

    ActionResult onDamage(ServerPlayerEntity player, DamageSource source, float amount);
}
