package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;

record ServerPlayerSet(PlayerManager players) implements PlayerSet {
    @Override
    public boolean contains(UUID id) {
        return this.players.getPlayer(id) != null;
    }

    @Override
    @Nullable
    public ServerPlayerEntity getEntity(UUID id) {
        return this.players.getPlayer(id);
    }

    @Override
    public int size() {
        return this.players.getPlayerList().size();
    }

    @Override
    public Iterator<ServerPlayerEntity> iterator() {
        return this.players.getPlayerList().iterator();
    }
}
