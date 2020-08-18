package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class GameLifecycle {
    private final List<Listeners> listeners = new ArrayList<>();

    public void addListeners(Listeners listeners) {
        this.listeners.add(listeners);
    }

    void addPlayer(GameWorld gameWorld, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onAddPlayer(gameWorld, player);
        }
    }

    void removePlayer(GameWorld gameWorld, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onRemovePlayer(gameWorld, player);
        }
    }

    void close(GameWorld gameWorld, List<ServerPlayerEntity> players) {
        for (Listeners listener : this.listeners) {
            listener.onClose(gameWorld, players);
        }
    }

    public interface Listeners {
        void onAddPlayer(GameWorld gameWorld, ServerPlayerEntity player);

        void onRemovePlayer(GameWorld gameWorld, ServerPlayerEntity player);

        void onClose(GameWorld gameWorld, List<ServerPlayerEntity> players);
    }
}
