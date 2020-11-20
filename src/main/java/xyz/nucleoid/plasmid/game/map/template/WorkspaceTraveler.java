package xyz.nucleoid.plasmid.game.map.template;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface WorkspaceTraveler {
    @Nullable
    static ReturnPosition getReturnFor(ServerPlayerEntity player, RegistryKey<World> dimension) {
        if (player instanceof WorkspaceTraveler) {
            return ((WorkspaceTraveler) player).getReturnFor(dimension);
        }
        return null;
    }

    @Nullable
    static ReturnPosition getLeaveReturn(ServerPlayerEntity player) {
        if (player instanceof WorkspaceTraveler) {
            return ((WorkspaceTraveler) player).getLeaveReturn();
        }
        return null;
    }

    @Nullable
    ReturnPosition getReturnFor(RegistryKey<World> dimension);

    @Nullable
    ReturnPosition getLeaveReturn();
}
