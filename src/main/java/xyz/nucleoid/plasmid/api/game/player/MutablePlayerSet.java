package xyz.nucleoid.plasmid.api.game.player;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class MutablePlayerSet implements PlayerSet {
    private final Function<UUID, ServerPlayer> playerGetter;

    private final Set<UUID> players = new ObjectOpenHashSet<>();

    public MutablePlayerSet(MinecraftServer server) {
        this.playerGetter = server.getPlayerList()::getPlayer;
    }

    public MutablePlayerSet(ServerLevel level) {
        this.playerGetter = (uuid) -> level.getPlayerByUUID(uuid) instanceof ServerPlayer player ? player : null;
    }

    public MutablePlayerSet(GameSpace gameSpace) {
        this.playerGetter = gameSpace.getPlayers()::getEntity;
    }

    public void clear() {
        this.players.clear();
    }

    public boolean add(ServerPlayer player) {
        return this.players.add(player.getUUID());
    }

    public boolean add(PlayerRef ref) {
        return this.players.add(ref.id());
    }

    public boolean remove(ServerPlayer player) {
        return this.players.remove(player.getUUID());
    }

    public boolean remove(PlayerRef ref) {
        return this.players.remove(ref.id());
    }

    public boolean remove(UUID id) {
        return this.players.remove(id);
    }

    @Nullable
    @Override
    public ServerPlayer getEntity(UUID id) {
        return this.players.contains(id) ? this.playerGetter.apply(id) : null;
    }

    @Override
    public boolean contains(UUID id) {
        return this.players.contains(id);
    }

    @Override
    public @NotNull Iterator<ServerPlayer> iterator() {
        var playerGetter = this.playerGetter;
        var ids = this.players.iterator();

        return new AbstractIterator<>() {
            @Override
            protected ServerPlayer computeNext() {
                while (ids.hasNext()) {
                    var id = ids.next();
                    var player = playerGetter.apply(id);
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
