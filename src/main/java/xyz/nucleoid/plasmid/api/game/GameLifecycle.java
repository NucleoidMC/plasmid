package xyz.nucleoid.plasmid.api.game;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.impl.game.manager.ManagedGameActivity;

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

    public void beforeActivityChange(GameSpace gameSpace, GameActivity activity, @Nullable GameActivity closedActivity) {
        for (var listener : this.listeners) {
            listener.beforeActivityChange(gameSpace, activity, closedActivity);
        }
    }

    public void afterActivityChange(GameSpace gameSpace, ManagedGameActivity activity, @Nullable ManagedGameActivity closedActivity) {
        for (var listener : this.listeners) {
            listener.afterActivityChange(gameSpace, activity, closedActivity);
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

        default void beforeActivityChange(GameSpace gameSpace, GameActivity activity, @Nullable GameActivity closedActivity) {
        }

        default void afterActivityChange(GameSpace gameSpace, GameActivity activity, @Nullable GameActivity closedActivity) {
        }
    }
}
