package xyz.nucleoid.plasmid.api.game;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

/**
 * Utility class containing various functions that supply {@link Component} instances.
 * <p>
 * This provides a common path for Plasmid and game implementations to share common messages.
 */
public final class GameComponents {
    public static MutableComponent commandLink(String text, String command) {
        return Component.literal(text).setStyle(commandLinkStyle(command));
    }

    public static MutableComponent commandLink(Component text, String command) {
        return text.copy().setStyle(commandLinkStyle(command));
    }

    public static Style commandLinkStyle(String command) {
        return commandLinkStyle(command, Component.literal(command));
    }

    public static Style commandLinkStyle(String command, Component hoverText) {
        return Style.EMPTY
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(hoverText))
                .applyFormats(ChatFormatting.BLUE, ChatFormatting.UNDERLINE);
    }

    public static final class Broadcast {
        public static MutableComponent gameOpened(CommandSourceStack source, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().withStyle(ChatFormatting.GRAY);

            return Component.translatable("text.plasmid.game.open.opened", source.getDisplayName(), gameName)
                    .append(GameComponents.Join.link(gameSpace));
        }

        public static MutableComponent gameOpenedTesting(CommandSourceStack source, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().withStyle(ChatFormatting.GRAY);

            return Component.translatable("text.plasmid.game.open.opened.testing", source.getDisplayName(), gameName)
                    .append(GameComponents.Join.link(gameSpace));
        }

        public static MutableComponent propose(CommandSourceStack source, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().withStyle(ChatFormatting.GRAY);

            return Component.translatable("text.plasmid.game.propose", source.getDisplayName(), gameName)
                    .append(GameComponents.Join.link(gameSpace));
        }

        public static MutableComponent gameOpenError() {
            return Component.translatable("text.plasmid.game.open.error");
        }
    }

    public static final class Command {
        public static MutableComponent located(ServerPlayer player, GameSpace gameSpace) {
            var gameName = GameConfig.name(gameSpace.getMetadata().sourceConfig()).copy().withStyle(ChatFormatting.GRAY);

            return Component.translatable("text.plasmid.game.locate.located", player.getDisplayName(), gameName)
                    .append(GameComponents.Join.link(gameSpace));
        }

        public static MutableComponent gameList() {
            return Component.translatable("text.plasmid.game.list");
        }

        public static MutableComponent listEntry(Component entry) {
            return Component.translatable("text.plasmid.entry", entry);
        }
    }

    public static final class Start {
        public static MutableComponent genericError() {
            return Component.translatable("text.plasmid.game.start_result.generic_error");
        }

        public static MutableComponent alreadyStarted() {
            return Component.translatable("text.plasmid.game.start_result.already_started");
        }

        public static MutableComponent notEnoughPlayers() {
            return Component.translatable("text.plasmid.game.start_result.not_enough_players");
        }

        public static MutableComponent startedBy(CommandSourceStack source) {
            return Component.translatable("text.plasmid.game.started.player", source.getDisplayName());
        }
    }

    public static final class Stop {
        public static MutableComponent stoppedBy(CommandSourceStack source) {
            return Component.translatable("text.plasmid.game.stopped.player", source.getDisplayName());
        }

        public static MutableComponent confirmStop() {
            return Component.translatable("text.plasmid.game.stop.confirm");
        }

        public static MutableComponent genericError() {
            return Component.translatable("text.plasmid.game.stopped.error");
        }
    }

    public static final class Join {
        public static MutableComponent success(ServerPlayer player) {
            return Component.translatable("text.plasmid.game.join", player.getDisplayName());
        }

        public static MutableComponent successSpectator(ServerPlayer player) {
            return Component.translatable("text.plasmid.game.join.spectate", player.getDisplayName());
        }

        public static MutableComponent link(GameSpace gameSpace) {
            var hover = Component.translatable("text.plasmid.join_link_hover", GameConfig.name(gameSpace.getMetadata().sourceConfig()));

            return Component.translatable("text.plasmid.game.open.join")
                    .setStyle(commandLinkStyle("/game join " + gameSpace.getMetadata().userId(), hover));
        }

        public static MutableComponent partyJoinError(int errorCount) {
            return Component.translatable("text.plasmid.game.join.party.error", errorCount);
        }

        public static MutableComponent genericError() {
            return Component.translatable("text.plasmid.join_result.generic_error");
        }

        public static MutableComponent unexpectedError() {
            return Component.translatable("text.plasmid.join_result.error");
        }

        public static MutableComponent gameClosed() {
            return Component.translatable("text.plasmid.join_result.game_closed");
        }

        public static MutableComponent gameFull() {
            return Component.translatable("text.plasmid.join_result.game_full");
        }

        public static MutableComponent alreadyJoined() {
            return Component.translatable("text.plasmid.join_result.already_joined");
        }

        public static MutableComponent inOtherGame() {
            return Component.translatable(
                    "text.plasmid.join_result.in_other_game",
                    commandLink(
                            Component.translatable("text.plasmid.join_result.in_other_game.leave_this_game"),
                            "/game leave"
                    )
            );
        }

        public static MutableComponent notAllowed() {
            return Component.translatable("text.plasmid.join_result.not_allowed");
        }

        public static MutableComponent spectatorsOnly() {
            return Component.translatable("text.plasmid.join_result.spectators_only");
        }

        public static MutableComponent participantsOnly() {
            return Component.translatable("text.plasmid.join_result.participants_only");
        }
    }

    public static final class Leave {
        public static MutableComponent participant(ServerPlayer player) {
            return Component.translatable("text.plasmid.game.leave", player.getDisplayName());
        }

        public static MutableComponent spectator(ServerPlayer player) {
            return Component.translatable("text.plasmid.game.leave.spectate", player.getDisplayName());
        }
    }


    public static final class Kick {
        public static MutableComponent kick(CommandSourceStack source, ServerPlayer target) {
            return source.isPlayer() ? kickBy(source.getPlayer(), target) : kick(target);
        }

        public static MutableComponent kickBy(ServerPlayer source, ServerPlayer target) {
            return Component.translatable("text.plasmid.game.kick.by", target.getDisplayName(), source.getDisplayName());
        }

        public static MutableComponent kick(ServerPlayer target) {
            return Component.translatable("text.plasmid.game.kick", target.getDisplayName());
        }
    }
}
