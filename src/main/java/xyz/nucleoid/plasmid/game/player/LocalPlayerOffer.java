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

public record LocalPlayerOffer(ServerPlayerEntity player) implements PlayerOffer {
    @Override
    public GameProfile profile() {
        return this.player.getGameProfile();
    }

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

        private final List<Consumer<ServerPlayerEntity>> thenRun = new ArrayList<>();

        Accept(ServerWorld world, Vec3d position) {
            this.world = world;
            this.position = position;
        }

        @Override
        public Accept thenRun(Consumer<ServerPlayerEntity> consumer) {
            this.thenRun.add(consumer);
            return this;
        }

        public ServerWorld applyJoin(ServerPlayerEntity player) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.refreshPositionAndAngles(this.position.x, this.position.y, this.position.z, 0.0F, 0.0F);

            for (Consumer<ServerPlayerEntity> consumer : this.thenRun) {
                consumer.accept(player);
            }

            return this.world;
        }
    }
}
