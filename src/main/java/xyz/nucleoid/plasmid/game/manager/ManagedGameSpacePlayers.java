package xyz.nucleoid.plasmid.game.manager;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpacePlayers;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public final class ManagedGameSpacePlayers implements GameSpacePlayers {
    private final ManagedGameSpace space;
    private final MutablePlayerSet set;

    ManagedGameSpacePlayers(ManagedGameSpace space) {
        this.space = space;
        this.set = new MutablePlayerSet(space.getServer());
    }

    @Override
    public GameResult screenJoins(Collection<ServerPlayerEntity> players) {
        return this.space.screenJoins(players);
    }

    @Override
    public GameResult offer(ServerPlayerEntity player) {
        return this.space.offer(player);
    }

    @Override
    public boolean kick(ServerPlayerEntity player) {
        return this.space.kick(player);
    }

    public void add(ServerPlayerEntity player) {
        this.set.add(player);
    }

    public boolean remove(ServerPlayerEntity player) {
        return this.set.remove(player);
    }

    public void clear() {
        this.set.clear();
    }

    @Override
    public boolean contains(UUID id) {
        return this.set.contains(id);
    }

    @Override
    @Nullable
    public ServerPlayerEntity getEntity(UUID id) {
        return this.set.getEntity(id);
    }

    @Override
    public int size() {
        return this.set.size();
    }

    @Override
    public Iterator<ServerPlayerEntity> iterator() {
        return this.set.iterator();
    }
}
