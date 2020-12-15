package xyz.nucleoid.plasmid.game;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletionException;

public class GameOpenException extends RuntimeException {
    private final Text reason;

    public GameOpenException(Text reason) {
        super(reason.getString());
        this.reason = reason;
    }

    public GameOpenException(Text reason, Throwable cause) {
        super(reason.getString(), cause);
        this.reason = reason;
    }

    public Text getReason() {
        return this.reason;
    }

    @Nullable
    public static GameOpenException unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException) {
            return unwrap(throwable.getCause());
        } else if (throwable instanceof GameOpenException) {
            return (GameOpenException) throwable;
        }
        return null;
    }
}
