package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface PlayerOfferResult {
    @Nullable
    default Accept asAccept() {
        return null;
    }

    @Nullable
    default Reject asReject() {
        return null;
    }

    boolean isTerminal();

    final class Pass implements PlayerOfferResult {
        static final Pass INSTANCE = new Pass();

        private Pass() {
        }

        @Override
        public boolean isTerminal() {
            return false;
        }
    }

    final class Accept implements PlayerOfferResult {
        public ServerWorld getWorld() {
            return this.world;
        }

        public Vec3d getPosition() {
            return this.position;
        }

        private final ServerWorld world;
        private final Vec3d position;

        private final List<Runnable> and = new ArrayList<>();

        Accept(ServerWorld world, Vec3d position) {
            this.world = world;
            this.position = position;
        }

        public PlayerOfferResult.Accept and(Runnable and) {
            this.and.add(and);
            return this;
        }

        public ServerWorld applyJoin(ServerPlayerEntity player) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.refreshPositionAndAngles(this.position.x, this.position.y, this.position.z, 0.0F, 0.0F);

            this.and.forEach(Runnable::run);

            return this.world;
        }

        public void run()
        {
            this.and.forEach(Runnable::run);
        }

        @Override
        public Accept asAccept() {
            return this;
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    }

    record Reject(Text reason) implements PlayerOfferResult {
        @Override
        public Reject asReject() {
            return this;
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    }
}
