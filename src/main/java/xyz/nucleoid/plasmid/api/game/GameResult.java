package xyz.nucleoid.plasmid.api.game;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Describes the result from a user-involved action with a {@link GameSpace}.
 * The result can either represent that the action was performed successfully ({@link GameResult#ok()}) or that an
 * issue occurred while trying to perform this action ({@link GameResult#error(Component)}).
 */
public final class GameResult {
    private static final GameResult OK = new GameResult(null);

    private final Component error;

    private GameResult(Component error) {
        this.error = error;
    }

    public static GameResult ok() {
        return OK;
    }

    /**
     * Returns a {@link GameResult} with the given {@link Component} as an error.
     *
     * @param error error in {@link Component} format
     * @return {@link GameResult} with the given error
     */
    public static GameResult error(Component error) {
        Preconditions.checkNotNull(error, "error must not be null");
        return new GameResult(error);
    }

    /**
     * @return whether this {@link GameResult} is a success (no error).
     */
    public boolean isOk() {
        return this.error == null;
    }

    /**
     * @return whether this {@link GameResult} contains an error
     */
    public boolean isError() {
        return this.error != null;
    }

    /**
     * Returns the error result of this {@link GameResult} as a {@link Component}.
     *
     * <p>If no error occurred, null is returned.
     *
     * @return the error of this {@link GameResult}, or null if one does not exist
     */
    @Nullable
    public Component error() {
        return this.error;
    }

    /**
     * Returns the error result of this {@link GameResult} as a copied {@link MutableComponent}.
     *
     * <p>If no error occurred, null is returned.
     *
     * @return the error of this {@link GameResult}, or null if one does not exist
     */
    @Nullable
    public MutableComponent errorCopy() {
        var error = this.error;
        return error != null ? error.copy() : null;
    }
}
