package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

// TODO: replace with normal events?
public final class GameLifecycle {
    private final List<Listeners> listeners = new ArrayList<>();

    public void addListeners(Listeners listeners) {
        this.listeners.add(listeners);
    }

    public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onAddPlayer(gameSpace, player);
        }
    }

    public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        for (Listeners listener : this.listeners) {
            listener.onRemovePlayer(gameSpace, player);
        }
    }

    public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
        for (Listeners listener : this.listeners) {
            listener.onClosing(gameSpace, reason);
        }
    }

    public void onClosed(GameSpace gameSpace, List<ServerPlayerEntity> players, GameCloseReason reason) {
        for (Listeners listener : this.listeners) {
            listener.onClosed(gameSpace, players, reason);
        }
    }

    public interface Listeners {
        default void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        }

        default void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        }

        default void onClosing(GameSpace gameSpace, GameCloseReason reason) {
        }

        default void onClosed(GameSpace gameSpace, List<ServerPlayerEntity> players, GameCloseReason reason) {
        }
    }
}
