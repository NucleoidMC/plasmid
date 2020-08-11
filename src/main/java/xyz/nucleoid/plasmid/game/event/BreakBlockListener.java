package xyz.nucleoid.plasmid.game.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface BreakBlockListener {
    EventType<BreakBlockListener> EVENT = EventType.create(BreakBlockListener.class, listeners -> {
        return (player, pos) -> {
            for (BreakBlockListener listener : listeners) {
                if (listener.onBreak(player, pos)) {
                    return true;
                }
            }
            return false;
        };
    });

    boolean onBreak(ServerPlayerEntity player, BlockPos pos);
}
