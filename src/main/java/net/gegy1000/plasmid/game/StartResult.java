package net.gegy1000.plasmid.game;

import com.google.common.base.Preconditions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import javax.annotation.Nullable;

public final class StartResult {
    private final Text error;

    private StartResult(Text error) {
        this.error = error;
    }

    public static StartResult ok() {
        return new StartResult(null);
    }

    public static StartResult err(Text error) {
        Preconditions.checkNotNull(error, "error must not be null");
        return new StartResult(error);
    }

    public static StartResult notEnoughPlayers() {
        return err(new LiteralText("Game does not have enough players yet!"));
    }

    public static StartResult alreadyStarted() {
        return err(new LiteralText("This game has already started!"));
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
