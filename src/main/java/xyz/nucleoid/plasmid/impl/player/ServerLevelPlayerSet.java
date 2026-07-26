package xyz.nucleoid.plasmid.impl.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;

import java.util.Iterator;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record ServerLevelPlayerSet(ServerLevel world) implements PlayerSet {
    @Override
    public boolean contains(UUID id) {
        return this.world.getPlayerByUUID(id) != null;
    }

    @Override
    @Nullable
    public ServerPlayer getEntity(UUID id) {
        return (ServerPlayer) this.world.getPlayerByUUID(id);
    }

    @Override
    public int size() {
        return this.world.players().size();
    }

    @Override
    public @NotNull Iterator<ServerPlayer> iterator() {
        return this.world.players().iterator();
    }
}
