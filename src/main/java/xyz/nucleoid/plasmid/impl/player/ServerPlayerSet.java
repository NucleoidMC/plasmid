package xyz.nucleoid.plasmid.impl.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;

import java.util.Iterator;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public record ServerPlayerSet(PlayerList players) implements PlayerSet {
    @Override
    public boolean contains(UUID id) {
        return this.players.getPlayer(id) != null;
    }

    @Override
    @Nullable
    public ServerPlayer getEntity(UUID id) {
        return this.players.getPlayer(id);
    }

    @Override
    public int size() {
        return this.players.getPlayers().size();
    }

    @Override
    public @NotNull Iterator<ServerPlayer> iterator() {
        return this.players.getPlayers().iterator();
    }
}
