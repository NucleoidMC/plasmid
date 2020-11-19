package xyz.nucleoid.plasmid.game.player;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public final class MutablePlayerSet implements PlayerSet {
    private final MinecraftServer server;

    private final Set<UUID> players = new ObjectOpenHashSet<>();

    public MutablePlayerSet(MinecraftServer server) {
        this.server = server;
    }

    public void clear() {
        this.players.clear();
    }

    public boolean add(ServerPlayerEntity player) {
        return this.players.add(player.getUuid());
    }

    public boolean remove(ServerPlayerEntity player) {
        return this.players.remove(player.getUuid());
    }

    @Nullable
    @Override
    public ServerPlayerEntity getEntity(UUID id) {
        return this.players.contains(id) ? this.server.getPlayerManager().getPlayer(id) : null;
    }

    @Override
    public boolean contains(UUID id) {
        return this.players.contains(id);
    }

    @Override
    public Iterator<ServerPlayerEntity> iterator() {
        PlayerManager playerManager = this.server.getPlayerManager();
        Iterator<UUID> ids = this.players.iterator();

        return new AbstractIterator<ServerPlayerEntity>() {
            @Override
            protected ServerPlayerEntity computeNext() {
                while (true) {
                    if (!ids.hasNext()) {
                        return this.endOfData();
                    }

                    UUID id = ids.next();
                    ServerPlayerEntity player = playerManager.getPlayer(id);
                    if (player != null) {
                        return player;
                    }
                }
            }
        };
    }

    @Override
    public int size() {
        return this.players.size();
    }

    @Override
    public MutablePlayerSet copy() {
        MutablePlayerSet copy = new MutablePlayerSet(this.server);
        copy.players.addAll(this.players);
        return copy;
    }
}
