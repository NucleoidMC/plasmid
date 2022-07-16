package xyz.nucleoid.plasmid.game;

import com.google.common.base.Preconditions;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * Describes the result from a user-involved action with a {@link GameSpace}.
 * The result can either represent that the action was performed successfully ({@link GameResult#ok()}) or that an
 * issue occurred while trying to perform this action ({@link GameResult#error(Text)}).
 */
public final class GameResult {
    private static final GameResult OK = new GameResult(null);

    private final Text error;

    private GameResult(Text error) {
        this.error = error;
    }

    public static GameResult ok() {
        return OK;
    }

    /**
     * Returns a {@link GameResult} with the given {@link Text} as an error.
     *
     * @param error error in {@link Text} format
     * @return {@link GameResult} with the given error
     */
    public static GameResult error(Text error) {
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
     * Returns the error result of this {@link GameResult} as a {@link Text}.
     *
     * <p>If no error occurred, null is returned.
     *
     * @return the error of this {@link GameResult}, or null if one does not exist
     */
    @Nullable
    public Text error() {
        return this.error;
    }

    /**
     * Returns the error result of this {@link GameResult} as a copied {@link MutableText}.
     *
     * <p>If no error occurred, null is returned.
     *
     * @return the error of this {@link GameResult}, or null if one does not exist
     */
    @Nullable
    public MutableText errorCopy() {
        var error = this.error;
        return error != null ? error.copy() : null;
    }
}
