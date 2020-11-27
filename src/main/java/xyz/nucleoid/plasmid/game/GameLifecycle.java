package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class GameLifecycle {
    private final List<Listeners> listeners = new ArrayList<>();

    public void addListeners(Listeners listeners) {
        this.listeners.add(listeners);
    }

    void addPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onAddPlayer(gameSpace, player);
        }
    }

    void removePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onRemovePlayer(gameSpace, player);
        }
    }

    void close(GameSpace gameSpace, List<ServerPlayerEntity> players, GameCloseReason reason) {
        for (Listeners listener : this.listeners) {
            listener.onClose(gameSpace, players, reason);
        }
    }

    public interface Listeners {
        default void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        }

        default void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        }

        default void onClose(GameSpace gameSpace, List<ServerPlayerEntity> players, GameCloseReason reason) {
        }
    }
}
