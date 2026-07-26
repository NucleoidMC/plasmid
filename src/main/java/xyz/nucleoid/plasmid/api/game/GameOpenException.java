package xyz.nucleoid.plasmid.api.game;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletionException;
import net.minecraft.network.chat.Component;

public class GameOpenException extends RuntimeException {
    private final Component reason;

    public GameOpenException(Component reason) {
        super(reason.getString());
        this.reason = reason;
    }

    public GameOpenException(Component reason, Throwable cause) {
        super(reason.getString(), cause);
        this.reason = reason;
    }

    public Component getReason() {
        return this.reason;
    }

    @Nullable
    public static GameOpenException unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException) {
            return unwrap(throwable.getCause());
        } else if (throwable instanceof GameOpenException unwrap) {
            return unwrap;
        }
        return null;
    }
}
