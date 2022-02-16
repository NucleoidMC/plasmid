package xyz.nucleoid.plasmid.game;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GameLifecycle {
    private final List<Listeners> listeners = new ArrayList<>();

    public void addListeners(Listeners listeners) {
        this.listeners.add(listeners);
    }

    public void onAddPlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        for (var listener : this.listeners) {
            listener.onAddPlayer(gameSpace, player);
        }
    }

    public void onRemovePlayer(GameSpace gameSpace, ServerPlayerEntity player) {
        for (var listener : this.listeners) {
            listener.onRemovePlayer(gameSpace, player);
        }
    }

    public void onClosing(GameSpace gameSpace, GameCloseReason reason) {
        for (var listener : this.listeners) {
            listener.onClosing(gameSpace, reason);
        }
    }

    public void onClosed(GameSpace gameSpace, List<ServerPlayerEntity> players, GameCloseReason reason) {
        for (var listener : this.listeners) {
            listener.onClosed(gameSpace, players, reason);
        }
    }

    public void onError(GameSpace gameSpace, Throwable throwable, String context) {
        for (var listener : this.listeners) {
            listener.onError(gameSpace, throwable, context);
        }
    }

    public void onActivityChange(GameSpace gameSpace, GameActivity newActivity, @Nullable GameActivity oldActivity) {
        for (var listener : this.listeners) {
            listener.onActivityChange(gameSpace, newActivity, oldActivity);
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

        default void onError(GameSpace gameSpace, Throwable throwable, String context) {
        }

        default void onActivityChange(GameSpace gameSpace, GameActivity newActivity, @Nullable GameActivity oldActivity) {
        }
    }
}
