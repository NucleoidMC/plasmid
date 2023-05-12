package xyz.nucleoid.plasmid.game.player;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.util.PlayerRef;

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

    public boolean add(PlayerRef ref) {
        return this.players.add(ref.id());
    }

    public boolean remove(ServerPlayerEntity player) {
        return this.players.remove(player.getUuid());
    }

    public boolean remove(PlayerRef ref) {
        return this.players.remove(ref.id());
    }

    public boolean remove(UUID id) {
        return this.players.remove(id);
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
    public @NotNull Iterator<ServerPlayerEntity> iterator() {
        var playerManager = this.server.getPlayerManager();
        var ids = this.players.iterator();

        return new AbstractIterator<>() {
            @Override
            protected ServerPlayerEntity computeNext() {
                while (ids.hasNext()) {
                    var id = ids.next();
                    var player = playerManager.getPlayer(id);
                    if (player != null) {
                        return player;
                    }
                }
                return this.endOfData();
            }
        };
    }

    @Override
    public int size() {
        return this.players.size();
    }
}
