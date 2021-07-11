package xyz.nucleoid.plasmid.map.creation.workspace;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface WorkspaceTraveler {
    @Nullable
    static ReturnPosition getReturnFor(ServerPlayerEntity player, RegistryKey<World> dimension) {
        if (player instanceof WorkspaceTraveler traveler) {
            return traveler.getReturnFor(dimension);
        }
        return null;
    }

    @Nullable
    static ReturnPosition getLeaveReturn(ServerPlayerEntity player) {
        if (player instanceof WorkspaceTraveler traveler) {
            return traveler.getLeaveReturn();
        }
        return null;
    }

    @Nullable
    ReturnPosition getReturnFor(RegistryKey<World> dimension);

    @Nullable
    ReturnPosition getLeaveReturn();
}
