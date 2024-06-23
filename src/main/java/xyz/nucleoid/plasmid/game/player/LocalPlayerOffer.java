package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public record LocalPlayerOffer(ServerPlayerEntity player) implements PlayerOffer {
    @Override
    public PlayerOfferResult.Accept accept(ServerWorld world, Vec3d position) {
        return new Accept(world, position);
    }

    @Override
    public PlayerOfferResult.Reject reject(Text reason) {
        return () -> reason;
    }

    public static class Accept implements PlayerOfferResult.Accept {
        private final ServerWorld world;
        private final Vec3d position;

        private final List<Runnable> and = new ArrayList<>();

        Accept(ServerWorld world, Vec3d position) {
            this.world = world;
            this.position = position;
        }

        @Override
        public Accept and(Runnable and) {
            this.and.add(and);
            return this;
        }

        public ServerWorld applyJoin(ServerPlayerEntity player) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.refreshPositionAndAngles(this.position.x, this.position.y, this.position.z, 0.0F, 0.0F);

            this.and.forEach(Runnable::run);

            return this.world;
        }
    }
}
