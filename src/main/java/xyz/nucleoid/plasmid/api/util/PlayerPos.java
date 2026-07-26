package xyz.nucleoid.plasmid.api.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record PlayerPos(ServerLevel world, double x, double y, double z, float yaw, float pitch) {

    public PlayerPos(ServerLevel world, Vec3 position, float yaw, float pitch) {
        this(world, position.x, position.y, position.z, yaw, pitch);
    }
}
