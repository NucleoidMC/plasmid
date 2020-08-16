package xyz.nucleoid.plasmid.game;

import com.google.common.base.Preconditions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * Describes the result of a {@link GameWorld} attempting to start.
 */
public final class StartResult {
    public static final StartResult ALREADY_STARTED = error(new LiteralText("This game has already started!"));
    public static final StartResult NOT_ENOUGH_PLAYERS = error(new LiteralText("Game does not have enough players yet!"));
    public static final StartResult OK = new StartResult(null);

    private final Text error;

    private StartResult(Text error) {
        this.error = error;
    }

    /**
     * Returns a {@link StartResult} with the given {@link Text} as an error.
     *
     * @param error error in {@link Text} format
     * @return {@link StartResult} with the given error
     */
    public static StartResult error(Text error) {
        Preconditions.checkNotNull(error, "error must not be null");
        return new StartResult(error);
    }

    /**
     * @return whether this {@link StartResult} is a success (no error).
     */
    public boolean isOk() {
        return this.error == null;
    }

    /**
     * @return whether this {@link StartResult} contains an error
     */
    public boolean isError() {
        return this.error != null;
    }

    /**
     * Returns the error result of this {@link StartResult} as a {@link Text}.
     *
     * <p>If no error occurred, null is returned.
     *
     * @return the error of this {@link StartResult}, or null if one does not exist
     */
    @Nullable
    public Text getError() {
        return this.error;
    }
}
