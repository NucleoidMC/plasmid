package xyz.nucleoid.plasmid.game;

import net.minecraft.text.Text;

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
}
