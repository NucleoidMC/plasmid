package xyz.nucleoid.plasmid.game;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.config.GameConfig;

/**
 * Utility class containing various functions that supply {@link Text} instances.
 * <p>
 * This provides a common path for Plasmid and game implementations to share common messages.
 */
public final class GameTexts {
    public static MutableText commandLink(String text, String command) {
        return Text.literal(text).setStyle(commandLinkStyle(command));
    }

    public static MutableText commandLink(Text text, String command) {
        return text.copy().setStyle(commandLinkStyle(command));
    }

    public static Style commandLinkStyle(String command) {
        return commandLinkStyle(command, Text.literal(command));
    }

    public static Style commandLinkStyle(String command, Text hoverText) {
        return Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                .withFormatting(Formatting.BLUE, Formatting.UNDERLINE);
    }

    public static final class Broadcast {
        public static MutableText gameOpened(ServerCommandSource source, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().formatted(Formatting.GRAY);

            return Text.translatable("text.plasmid.game.open.opened", source.getDisplayName(), gameName)
                    .append(GameTexts.Join.link(gameSpace));
        }

        public static MutableText gameOpenedTesting(ServerCommandSource source, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().formatted(Formatting.GRAY);

            return Text.translatable("text.plasmid.game.open.opened.testing", source.getDisplayName(), gameName)
                    .append(GameTexts.Join.link(gameSpace));
        }

        public static MutableText propose(ServerCommandSource source, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().formatted(Formatting.GRAY);

            return Text.translatable("text.plasmid.game.propose", source.getDisplayName(), gameName)
                    .append(GameTexts.Join.link(gameSpace));
        }

        public static MutableText gameOpenError() {
            return Text.translatable("text.plasmid.game.open.error");
        }
    }

    public static final class Command {
        public static MutableText located(ServerPlayerEntity player, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().formatted(Formatting.GRAY);

            return Text.translatable("text.plasmid.game.locate.located", player.getDisplayName(), gameName)
                    .append(GameTexts.Join.link(gameSpace));
        }

        public static MutableText gameList() {
            return Text.translatable("text.plasmid.game.list");
        }

        public static MutableText listEntry(Text entry) {
            return Text.translatable("text.plasmid.entry", entry);
        }
    }

    public static final class Start {
        public static MutableText genericError() {
            return Text.translatable("text.plasmid.game.start_result.generic_error");
        }

        public static MutableText alreadyStarted() {
            return Text.translatable("text.plasmid.game.start_result.already_started");
        }

        public static MutableText notEnoughPlayers() {
            return Text.translatable("text.plasmid.game.start_result.not_enough_players");
        }

        public static MutableText startedBy(ServerCommandSource source) {
            return Text.translatable("text.plasmid.game.started.player", source.getDisplayName());
        }
    }

    public static final class Stop {
        public static MutableText stoppedBy(ServerCommandSource source) {
            return Text.translatable("text.plasmid.game.stopped.player", source.getDisplayName());
        }

        public static MutableText confirmStop() {
            return Text.translatable("text.plasmid.game.stop.confirm");
        }

        public static MutableText genericError() {
            return Text.translatable("text.plasmid.game.stopped.error");
        }
    }

    public static final class Join {
        public static MutableText success(ServerPlayerEntity player) {
            return Text.translatable("text.plasmid.game.join", player.getDisplayName());
        }

        public static MutableText link(GameSpace gameSpace) {
            var hover = Text.translatable("text.plasmid.join_link_hover", GameConfig.name(gameSpace.getMetadata().sourceConfig()));

            return Text.translatable("text.plasmid.game.open.join")
                    .setStyle(commandLinkStyle("/game join " + gameSpace.getMetadata().userId(), hover));
        }

        public static MutableText partyJoinError(int errorCount) {
            return Text.translatable("text.plasmid.game.join.party.error", errorCount);
        }

        public static MutableText genericError() {
            return Text.translatable("text.plasmid.join_result.generic_error");
        }

        public static MutableText unexpectedError() {
            return Text.translatable("text.plasmid.join_result.error");
        }

        public static MutableText gameClosed() {
            return Text.translatable("text.plasmid.join_result.game_closed");
        }

        public static MutableText gameFull() {
            return Text.translatable("text.plasmid.join_result.game_full");
        }

        public static MutableText alreadyJoined() {
            return Text.translatable("text.plasmid.join_result.already_joined");
        }

        public static MutableText inOtherGame() {
            return Text.translatable(
                    "text.plasmid.join_result.in_other_game",
                    commandLink(
                            Text.translatable("text.plasmid.join_result.in_other_game.leave_this_game"),
                            "/game leave"
                    )
            );
        }
    }

    public static final class Kick {
        public static MutableText kick(ServerCommandSource source, ServerPlayerEntity target) {
            return source.isExecutedByPlayer() ? kickBy(source.getPlayer(), target) : kick(target);
        }

        public static MutableText kickBy(ServerPlayerEntity source, ServerPlayerEntity target) {
            return Text.translatable("text.plasmid.game.kick.by", target.getDisplayName(), source.getDisplayName());
        }

        public static MutableText kick(ServerPlayerEntity target) {
            return Text.translatable("text.plasmid.game.kick", target.getDisplayName());
        }
    }
}
