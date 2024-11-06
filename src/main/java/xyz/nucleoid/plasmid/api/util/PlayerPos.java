package xyz.nucleoid.plasmid.api.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record PlayerPos(ServerWorld world, double x, double y, double z, float yaw, float pitch) {

    public PlayerPos(ServerWorld world, Vec3d position, float yaw, float pitch) {
        this(world, position.x, position.y, position.z, yaw, pitch);
    }
}
