package xyz.nucleoid.plasmid.game.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

final class EmptyPlayerSet implements PlayerSet {
    static final EmptyPlayerSet INSTANCE = new EmptyPlayerSet();

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
