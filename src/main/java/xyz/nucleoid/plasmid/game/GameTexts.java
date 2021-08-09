package xyz.nucleoid.plasmid.game;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

/**
 * Utility class containing various functions that supply {@link Text} instances.
 * <p>
 * This provides a common path for Plasmid and game implementations to share common messages.
 */
public final class GameTexts {
    public static MutableText commandLink(String text, String command) {
        return new LiteralText(text).setStyle(commandLinkStyle(command));
    }

    public static MutableText commandLink(Text text, String command) {
        return text.shallowCopy().setStyle(commandLinkStyle(command));
    }

    public static Style commandLinkStyle(String command) {
        return commandLinkStyle(command, new LiteralText(command));
    }

    public static Style commandLinkStyle(String command, Text hoverText) {
        return Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                .withFormatting(Formatting.BLUE, Formatting.UNDERLINE);
    }

    public static final class Broadcast {
        public static MutableText gameOpened(ServerCommandSource source, GameSpace gameSpace) {
            var gameName = gameSpace.getSourceConfig().getName().shallowCopy().formatted(Formatting.GRAY);

            return new TranslatableText("text.plasmid.game.open.opened", source.getDisplayName(), gameName)
                    .append(GameTexts.Join.link(gameSpace));
        }

        public static MutableText propose(ServerCommandSource source, GameSpace gameSpace) {
            var gameName = gameSpace.getSourceConfig().getName().shallowCopy().formatted(Formatting.GRAY);

            return new TranslatableText("text.plasmid.game.propose", source.getDisplayName(), gameName)
                    .append(GameTexts.Join.link(gameSpace));
        }

        public static MutableText gameOpenError() {
            return new TranslatableText("text.plasmid.game.open.error");
        }
    }

    public static final class Command {
        public static MutableText located(ServerPlayerEntity player, GameSpace gameSpace) {
            var gameName = gameSpace.getSourceConfig().getName().shallowCopy().formatted(Formatting.GRAY);

            return new TranslatableText("text.plasmid.game.locate.located", player.getDisplayName(), gameName)
                    .append(GameTexts.Join.link(gameSpace));
        }

        public static MutableText gameList() {
            return new TranslatableText("text.plasmid.game.list");
        }

        public static MutableText listEntry(Text entry) {
            return new TranslatableText("text.plasmid.entry", entry);
        }
    }

    public static final class Start {
        public static MutableText genericError() {
            return new TranslatableText("text.plasmid.game.start_result.generic_error");
        }

        public static MutableText alreadyStarted() {
            return new TranslatableText("text.plasmid.game.start_result.already_started");
        }

        public static MutableText notEnoughPlayers() {
            return new TranslatableText("text.plasmid.game.start_result.not_enough_players");
        }

        public static MutableText startedBy(ServerCommandSource source) {
            return new TranslatableText("text.plasmid.game.started.player", source.getDisplayName());
        }
    }

    public static final class Stop {
        public static MutableText stoppedBy(ServerCommandSource source) {
            return new TranslatableText("text.plasmid.game.stopped.player", source.getDisplayName());
        }

        public static MutableText confirmStop() {
            return new TranslatableText("text.plasmid.game.stop.confirm");
        }

        public static MutableText genericError() {
            return new TranslatableText("text.plasmid.game.stopped.error");
        }
    }

    public static final class Join {
        public static MutableText success(ServerPlayerEntity player) {
            return new TranslatableText("text.plasmid.game.join", player.getDisplayName());
        }

        public static MutableText link(GameSpace gameSpace) {
            var hover = new TranslatableText("text.plasmid.join_link_hover", gameSpace.getSourceConfig().getName());

            return new TranslatableText("text.plasmid.game.open.join")
                    .setStyle(commandLinkStyle("/game join " + gameSpace.getUserId(), hover));
        }

        public static MutableText partyJoinError(int errorCount) {
            return new TranslatableText("text.plasmid.game.join.party.error", errorCount);
        }

        public static MutableText genericError() {
            return new TranslatableText("text.plasmid.join_result.generic_error");
        }

        public static MutableText unexpectedError() {
            return new TranslatableText("text.plasmid.join_result.error");
        }

        public static MutableText gameClosed() {
            return new TranslatableText("text.plasmid.join_result.game_closed");
        }

        public static MutableText gameFull() {
            return new TranslatableText("text.plasmid.join_result.game_full");
        }

        public static MutableText alreadyJoined() {
            return new TranslatableText("text.plasmid.join_result.already_joined");
        }

        public static MutableText inOtherGame() {
            return new TranslatableText(
                    "text.plasmid.join_result.in_other_game",
                    commandLink(
                            new TranslatableText("text.plasmid.join_result.in_other_game.leave_this_game"),
                            "/game leave"
                    )
            );
        }
    }
}
