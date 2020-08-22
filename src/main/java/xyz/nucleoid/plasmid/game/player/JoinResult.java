package xyz.nucleoid.plasmid.game.player;

import com.google.common.base.Preconditions;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

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

    public static JoinResult inOtherGame() {
        String linkCommand = "/game leave";
        Style linkStyle = Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, linkCommand))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(linkCommand)))
                .withFormatting(Formatting.BLUE, Formatting.UNDERLINE);

        Text link = new LiteralText("leave this game")
                .setStyle(linkStyle);

        return err(new LiteralText("You must ").append(link).append(" before joining another game!"));
    }

    public boolean isOk() {
        return this.error == null;
    }

    public boolean isError() {
        return this.error != null;
    }

    @Nullable
    public Text getError() {
        return this.error;
    }
}
