package xyz.nucleoid.plasmid.api.game.player;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public sealed interface RespawnResult permits RespawnResult.Pass, RespawnResult.Respawn, RespawnResult.Leave {
    Pass PASS = new Pass();
    Leave LEAVE = new Leave();

    final class Pass implements RespawnResult {
        private Pass() {
        }
    }

    non-sealed interface Respawn extends RespawnResult {
        TeleportTarget target();

        static Respawn at(ServerWorld world, Vec3d pos) {
            return at(world, pos, new Vec3d(0, 0, 0));
        }
        static Respawn at(ServerWorld world, Vec3d pos, Vec3d velocity) {
            return at(world, pos, velocity, 0, 0);
        }
        static Respawn at(ServerWorld world, Vec3d pos, Vec3d velocity, float yaw, float pitch) {
            return at(new TeleportTarget(world, pos, velocity, yaw, pitch, TeleportTarget.NO_OP));
        }
        static Respawn at(TeleportTarget target) {
            return () -> target;
        }
    }

    final class Leave implements RespawnResult {
        private Leave() {
        }
    }
}
