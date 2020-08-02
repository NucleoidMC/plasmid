package net.gegy1000.plasmid.game;

import com.google.common.base.Preconditions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import javax.annotation.Nullable;

public final class JoinResult {
    private static final JoinResult OK = new JoinResult(null);

    private final Text error;

    private JoinResult(Text error) {
        this.error = error;
    }

    public static JoinResult ok() {
        return OK;
    }

    public static JoinResult err(Text error) {
        Preconditions.checkNotNull(error, "error must not be null");
        return new JoinResult(error);
    }

    public static JoinResult gameFull() {
        return err(new LiteralText("Game is already full!"));
    }

    public static JoinResult alreadyJoined() {
        return err(new LiteralText("You are already in this game!"));
    }

    public boolean isOk() {
        return this.error == null;
    }

    public boolean isErr() {
        return this.error != null;
    }

    @Nullable
    public Text getError() {
        return this.error;
    }
}
