package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class GameLifecycle {
    private final List<Listeners> listeners = new ArrayList<>();

    public void addListeners(Listeners listeners) {
        this.listeners.add(listeners);
    }

    void addPlayer(ManagedGameSpace gameSpace, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onAddPlayer(gameSpace, player);
        }
    }

    void removePlayer(ManagedGameSpace gameSpace, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onRemovePlayer(gameSpace, player);
        }
    }

    void close(ManagedGameSpace gameSpace, List<ServerPlayerEntity> players) {
        for (Listeners listener : this.listeners) {
            listener.onClose(gameSpace, players);
        }
    }

    public interface Listeners {
        void onAddPlayer(ManagedGameSpace gameSpace, ServerPlayerEntity player);

        void onRemovePlayer(ManagedGameSpace gameSpace, ServerPlayerEntity player);

        void onClose(ManagedGameSpace gameSpace, List<ServerPlayerEntity> players);
    }
}
