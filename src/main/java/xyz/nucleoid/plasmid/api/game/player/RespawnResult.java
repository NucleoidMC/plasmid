package xyz.nucleoid.plasmid.api.game.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public sealed interface RespawnResult permits RespawnResult.Pass, RespawnResult.Respawn, RespawnResult.Leave {
    Pass PASS = new Pass();
    Leave LEAVE = new Leave();

    final class Pass implements RespawnResult {
        private Pass() {
        }
    }

    non-sealed interface Respawn extends RespawnResult {
        TeleportTransition.PostTeleportTransition MARKER = (entity) -> {};

        TeleportTransition target();

        static Respawn at(ServerLevel world, Vec3 pos) {
            return at(world, pos, new Vec3(0, 0, 0));
        }
        static Respawn at(ServerLevel world, Vec3 pos, Vec3 velocity) {
            return at(world, pos, velocity, 0, 0);
        }
        static Respawn at(ServerLevel world, Vec3 pos, Vec3 velocity, float yaw, float pitch) {
            return at(new TeleportTransition(world, pos, velocity, yaw, pitch, MARKER));
        }
        static Respawn at(TeleportTransition target) {
            return () -> target;
        }
    }

    final class Leave implements RespawnResult {
        private Leave() {
        }
    }
}
