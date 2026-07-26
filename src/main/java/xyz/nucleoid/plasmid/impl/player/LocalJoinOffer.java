package xyz.nucleoid.plasmid.impl.player;

import com.mojang.authlib.GameProfile;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record LocalJoinOffer(Collection<ServerPlayer> serverPlayers, JoinIntent intent) implements JoinOffer {
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
    public JoinOfferResult.Reject reject(Component reason) {
        return () -> reason;
    }
}
