package xyz.nucleoid.plasmid.impl.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;

import java.util.Iterator;
import java.util.UUID;

public record ServerWorldPlayerSet(ServerWorld world) implements PlayerSet {
    @Override
    public boolean contains(UUID id) {
        return this.world.getPlayerByUuid(id) != null;
    }

    @Override
    @Nullable
    public ServerPlayerEntity getEntity(UUID id) {
        return (ServerPlayerEntity) this.world.getPlayerByUuid(id);
    }

    @Override
    public int size() {
        return this.world.getPlayers().size();
    }

    @Override
    public @NotNull Iterator<ServerPlayerEntity> iterator() {
        return this.world.getPlayers().iterator();
    }
}
