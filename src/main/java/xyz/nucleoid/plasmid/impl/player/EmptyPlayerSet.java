package xyz.nucleoid.plasmid.impl.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;

import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

public final class EmptyPlayerSet implements PlayerSet {
    public static final EmptyPlayerSet INSTANCE = new EmptyPlayerSet();

    EmptyPlayerSet() {
    }

    @Override
    public boolean contains(UUID id) {
        return false;
    }

    @Override
    @Nullable
    public ServerPlayerEntity getEntity(UUID id) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public MutablePlayerSet copy(MinecraftServer server) {
        return new MutablePlayerSet(server);
    }

    @NotNull
    @Override
    public Iterator<ServerPlayerEntity> iterator() {
        return Collections.emptyIterator();
    }
}
