package xyz.nucleoid.plasmid.game.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record LocalJoinOffer(Collection<ServerPlayerEntity> serverPlayers, JoinIntent intent) implements JoinOffer {
    @Override
    public Set<GameProfile> players() {
        return this.serverPlayers
                .stream()
                .map(PlayerEntity::getGameProfile)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UUID> playerIds() {
        return this.serverPlayers
                .stream()
                .map(player -> player.getGameProfile().getId())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> playerNames() {
        return this.serverPlayers
                .stream()
                .map(player -> player.getGameProfile().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public JoinOfferResult.Accept accept(ServerWorld world, Vec3d position, float yaw, float pitch) {
        return new Accept(world, position, yaw, pitch);
    }

    @Override
    public JoinOfferResult.Reject reject(Text reason) {
        return () -> reason;
    }

    public static class Accept implements JoinOfferResult.Accept {
        private final ServerWorld world;
        private final Vec3d position;
        private final float yaw;
        private final float pitch;

        private final List<Consumer<PlayerSet>> thenRun = new ArrayList<>();

        Accept(ServerWorld world, Vec3d position, float yaw, float pitch) {
            this.world = world;
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Override
        public Accept thenRun(Consumer<PlayerSet> consumer) {
            this.thenRun.add(consumer);
            return this;
        }

        public void joinAll(PlayerSet players) {
            for (var consumer : this.thenRun) {
                consumer.accept(players);
            }
        }

        public ServerWorld applyJoin(ServerPlayerEntity player) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.refreshPositionAndAngles(this.position.x, this.position.y, this.position.z, this.yaw, this.pitch);

            return this.world;
        }
    }
}
