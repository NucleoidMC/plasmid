package xyz.nucleoid.plasmid.impl.player;

import com.mojang.authlib.GameProfile;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.util.PlayerPos;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public record LocalJoinAcceptor(Collection<ServerPlayer> serverPlayers, JoinIntent intent) implements JoinAcceptor {
    @Override
    public Set<GameProfile> players() {
        return this.serverPlayers
                .stream()
                .map(Player::getGameProfile)
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
        if (this.serverPlayers.stream().anyMatch(player -> !positions.containsKey(player.getUUID()))) {
            throw new IllegalArgumentException("Positions for all players must be specified");
        }
        return new LocalJoinAcceptor.Teleport(positions);
    }

    @Override
    public JoinAcceptorResult.Teleport teleport(Function<GameProfile, PlayerPos> positions) {
        return new LocalJoinAcceptor.Teleport(
                this.serverPlayers.stream().collect(Collectors.toMap(
                        ServerPlayer::getUUID,
                        player -> positions.apply(player.getGameProfile())
                ))
        );
    }

    @Override
    public JoinAcceptorResult.Teleport teleport(ServerLevel level, Vec3 position, float yaw, float pitch) {
        var playerPos = new PlayerPos(level, position, yaw, pitch);
        return new LocalJoinAcceptor.Teleport(
                this.serverPlayers.stream().collect(Collectors.toMap(
                        ServerPlayer::getUUID,
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

        public ServerLevel applyTeleport(ServerPlayer player) {
            var pos = this.positions.get(player.getUUID());

            player.setGameMode(GameType.SURVIVAL);
            player.snapTo(
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
