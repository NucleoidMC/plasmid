package xyz.nucleoid.plasmid.impl.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.util.PlayerPos;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public record LocalJoinAcceptor(Collection<ServerPlayerEntity> serverPlayers, JoinIntent intent) implements JoinAcceptor {
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
                .map(player -> player.getGameProfile().id())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> playerNames() {
        return this.serverPlayers
                .stream()
                .map(player -> player.getGameProfile().name())
                .collect(Collectors.toSet());
    }

    @Override
    public JoinAcceptorResult.Teleport teleport(Map<UUID, PlayerPos> positions) {
        if (this.serverPlayers.stream().anyMatch(player -> !positions.containsKey(player.getUuid()))) {
            throw new IllegalArgumentException("Positions for all players must be specified");
        }
        return new LocalJoinAcceptor.Teleport(positions);
    }

    @Override
    public JoinAcceptorResult.Teleport teleport(Function<GameProfile, PlayerPos> positions) {
        return new LocalJoinAcceptor.Teleport(
                this.serverPlayers.stream().collect(Collectors.toMap(
                        ServerPlayerEntity::getUuid,
                        player -> positions.apply(player.getGameProfile())
                ))
        );
    }

    @Override
    public JoinAcceptorResult.Teleport teleport(ServerWorld world, Vec3d position, float yaw, float pitch) {
        var playerPos = new PlayerPos(world, position, yaw, pitch);
        return new LocalJoinAcceptor.Teleport(
                this.serverPlayers.stream().collect(Collectors.toMap(
                        ServerPlayerEntity::getUuid,
                        player -> playerPos
                ))
        );
    }

    public static class Teleport implements JoinAcceptorResult.Teleport {
        private final Map<UUID, PlayerPos> positions;

        private final List<BiConsumer<PlayerSet, JoinIntent>> thenRun = new ArrayList<>();

        Teleport(Map<UUID, PlayerPos> positions) {
            this.positions = positions;
        }

        @Override
        public JoinAcceptorResult.Teleport thenRun(BiConsumer<PlayerSet, JoinIntent> consumer) {
            this.thenRun.add(consumer);
            return this;
        }

        public void runCallbacks(PlayerSet players, JoinIntent intent) {
            for (var consumer : this.thenRun) {
                consumer.accept(players, intent);
            }
        }

        public ServerWorld applyTeleport(ServerPlayerEntity player) {
            var pos = this.positions.get(player.getUuid());

            player.changeGameMode(GameMode.SURVIVAL);
            player.refreshPositionAndAngles(
                    pos.x(),
                    pos.y(),
                    pos.z(),
                    pos.yaw(),
                    pos.pitch()
            );

            return pos.world();
        }
    }
}
