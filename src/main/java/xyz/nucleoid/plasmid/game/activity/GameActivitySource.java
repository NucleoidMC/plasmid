package xyz.nucleoid.plasmid.game.activity;

import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.function.Consumer;

// TODO: name?
public interface GameActivitySource {
    void push(Consumer<GameActivity> builder);

    /**
     * Swaps out the active {@link GameActivity} within this {@link GameSpace}.
     *
     * @param builder the builder to apply on the newly constructed {@link GameActivity}
     */
    void swap(Consumer<GameActivity> builder);

    void pop(GameActivity activity, GameCloseReason reason);
}
