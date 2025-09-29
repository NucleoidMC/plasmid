package xyz.nucleoid.plasmid.impl.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;

import java.util.*;
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
    public JoinOfferResult.Reject reject(Text reason) {
        return () -> reason;
    }
}
