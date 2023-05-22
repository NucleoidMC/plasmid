package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        private final Consumer<ServerPlayerEntity> joinConsumer;
        private final List<Runnable> and = new ArrayList<>();

        Accept(ServerWorld world, Vec3d position) {
            this(player -> {
                player.setWorld(world);
                player.setPosition(position.x, position.y, position.z);
            });
        }

        Accept(Consumer<ServerPlayerEntity> joinProcessor)
        {
            this.joinConsumer = joinProcessor;
        }

        public PlayerOfferResult.Accept and(Runnable and) {
            this.and.add(and);
            return this;
        }

        public void applyJoin() {
            this.and.forEach(Runnable::run);
        }

        public void applyAccept(ServerPlayerEntity player) {
            this.joinConsumer.accept(player);
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
