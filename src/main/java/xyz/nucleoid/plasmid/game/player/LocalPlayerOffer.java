package xyz.nucleoid.plasmid.game.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record LocalPlayerOffer(ServerPlayerEntity player, JoinIntent intent) implements PlayerOffer {
    @Override
    public GameProfile profile() {
        return this.player.getGameProfile();
    }

    @Override
    public PlayerOfferResult.Accept accept(ServerWorld world, Vec3d position, float yaw, float pitch) {
        return new Accept(world, position, yaw, pitch);
    }

    @Override
    public PlayerOfferResult.Reject reject(Text reason) {
        return () -> reason;
    }

    public static class Accept implements PlayerOfferResult.Accept {
        private final ServerWorld world;
        private final Vec3d position;
        private final float yaw;
        private final float pitch;

        private final List<Consumer<ServerPlayerEntity>> thenRun = new ArrayList<>();

        Accept(ServerWorld world, Vec3d position, float yaw, float pitch) {
            this.world = world;
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Override
        public Accept thenRun(Consumer<ServerPlayerEntity> consumer) {
            this.thenRun.add(consumer);
            return this;
        }

        public ServerWorld applyJoin(ServerPlayerEntity player) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.refreshPositionAndAngles(this.position.x, this.position.y, this.position.z, this.yaw, this.pitch);

            for (Consumer<ServerPlayerEntity> consumer : this.thenRun) {
                consumer.accept(player);
            }

            return this.world;
        }
    }
}
