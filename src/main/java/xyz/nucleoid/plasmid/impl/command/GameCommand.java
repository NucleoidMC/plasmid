package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.command.argument.GameConfigArgument;
import xyz.nucleoid.plasmid.impl.command.argument.GameSpaceArgument;
import xyz.nucleoid.plasmid.impl.command.ui.GameJoinUi;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.GameTexts;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfigs;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.util.Scheduler;

import java.util.Comparator;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SimpleCommandExceptionType NO_GAME_OPEN = new SimpleCommandExceptionType(
            Text.translatable("text.plasmid.game.join.no_game_open")
    );

    public static final SimpleCommandExceptionType NOT_IN_GAME = new SimpleCommandExceptionType(
            Text.translatable("text.plasmid.game.not_in_game")
    );

    public static final DynamicCommandExceptionType MALFORMED_CONFIG = new DynamicCommandExceptionType(error ->
            Text.stringifiedTranslatable("text.plasmid.game.open.malformed_config", error)
    );

    public static final DynamicCommandExceptionType PLAYER_NOT_IN_GAME = new DynamicCommandExceptionType(player ->
            Text.stringifiedTranslatable("text.plasmid.game.locate.player_not_in_game", player)
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .requires(Permissions.require("plasmid.command.game.open", 2))
                    .then(GameConfigArgument.argument("game_config")
                        .executes(GameCommand::openGame)
                    )
                    .then(argument("game_config_nbt", NbtCompoundArgumentType.nbtCompound())
                        .executes(GameCommand::openAnonymousGame)
                    )
                )
                .then(literal("propose")
                    .requires(Permissions.require("plasmid.command.game.propose", 2))
                    .then(GameSpaceArgument.argument("game_space")
                        .executes(GameCommand::proposeGame)
                    )
                        .executes(GameCommand::proposeCurrentGame)
                )
                .then(literal("start")
                    .requires(Permissions.require("plasmid.command.game.start", 2))
                    .executes(GameCommand::startGame)
                )
                .then(literal("stop")
                    .requires(Permissions.require("plasmid.command.game.stop", 2))
                    .executes(GameCommand::stopGame)
                        .then(literal("confirm")
                            .executes(GameCommand::stopGameConfirmed)
                        )
                )
                .then(literal("kick")
                    .requires(Permissions.require("plasmid.command.game.kick", 2))
                    .then(argument("targets", EntityArgumentType.players())
                        .executes(GameCommand::kickPlayers)
                    )
                )
                .then(literal("join")
                    .executes(ctx -> GameCommand.joinGame(ctx, JoinIntent.PLAY))
                    .then(GameSpaceArgument.argument("game_space")
                        .executes(ctx -> GameCommand.joinQualifiedGame(ctx, JoinIntent.PLAY))
                    )
                )
                .then(literal("spectate")
                     .executes(ctx -> GameCommand.joinGame(ctx, JoinIntent.SPECTATE))
                     .then(GameSpaceArgument.argument("game_space")
                          .executes(ctx -> GameCommand.joinQualifiedGame(ctx, JoinIntent.SPECTATE))
                     )
                )
                .then(literal("joinall")
                    .requires(Permissions.require("plasmid.command.game.joinall", 2))
                    .executes(GameCommand::joinAllGame)
                    .then(GameSpaceArgument.argument("game_space")
                        .executes(GameCommand::joinAllQualifiedGame)
                    )
                )
                .then(literal("locate")
                        .then(argument("player", EntityArgumentType.player())
                        .executes(GameCommand::locatePlayer))
                )
                .then(literal("leave").executes(GameCommand::leaveGame))
                .then(literal("list").executes(GameCommand::listGames))
        );
    }
    // @formatter:on

    private static int openGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return openGame(context, false);
    }

    protected static int openGame(CommandContext<ServerCommandSource> context, boolean test) throws CommandSyntaxException {
        try {
            var game = GameConfigArgument.get(context, "game_config");
            return openGame(context, game, test);
        } catch (CommandSyntaxException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while opening a game", e);
            context.getSource().sendFeedback(() -> Text.translatable("text.plasmid.game.open.error").formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int openAnonymousGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return openAnonymousGame(context, false);
    }

    protected static int openAnonymousGame(CommandContext<ServerCommandSource> context, boolean test) throws CommandSyntaxException {
        try {
            var configNbt = NbtCompoundArgumentType.getNbtCompound(context, "game_config_nbt");
            var game = GameConfig.DIRECT_CODEC.parse(context.getSource().getRegistryManager().getOps(NbtOps.INSTANCE), configNbt)
                    .getOrThrow(MALFORMED_CONFIG::create);
            return openGame(context, RegistryEntry.of(game), test);
        } catch (CommandSyntaxException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while opening a game", e);
            context.getSource().sendFeedback(() -> Text.translatable("text.plasmid.game.open.error").formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int openGame(CommandContext<ServerCommandSource> context, RegistryEntry<GameConfig<?>> config, boolean test) {
        var source = context.getSource();
        var server = source.getServer();
        var player = source.getPlayer();

        if (player != null) {
            var currentGameSpace = GameSpaceManagerImpl.get().byPlayer(player);
            if (currentGameSpace != null) {
                if (test) {
                    currentGameSpace.close(GameCloseReason.CANCELED);
                } else {
                    currentGameSpace.getPlayers().kick(player);
                }
            }
        }

        GameSpaceManagerImpl.get().open(config).handleAsync((gameSpace, throwable) -> {
            if (throwable == null) {
                onOpenSuccess(source, gameSpace, player, test);
            } else {
                onOpenError(source, throwable);
            }
            return null;
        }, server);

        return Command.SINGLE_SUCCESS;
    }

    private static void onOpenSuccess(ServerCommandSource source, GameSpace gameSpace, ServerPlayerEntity player, boolean test) {
        var players = source.getServer().getPlayerManager();

        var message = test ? GameTexts.Broadcast.gameOpenedTesting(source, gameSpace) : GameTexts.Broadcast.gameOpened(source, gameSpace);
        players.broadcast(message, false);

        if (test) {
            joinAllPlayersToGame(source, gameSpace);

            var startResult = gameSpace.requestStart();

            if (!startResult.isOk()) {
                var error = startResult.errorCopy().formatted(Formatting.RED);
                gameSpace.getPlayers().sendMessage(error);
            }
        } else if (player != null) {
            tryJoinGame(player, gameSpace, JoinIntent.PLAY);
        }
    }

    private static void onOpenError(ServerCommandSource source, Throwable throwable) {
        Plasmid.LOGGER.error("Failed to start game", throwable);

        var gameOpenException = GameOpenException.unwrap(throwable);

        MutableText message;
        if (gameOpenException != null) {
            message = gameOpenException.getReason().copy();
        } else {
            message = GameTexts.Broadcast.gameOpenError();
        }

        var players = source.getServer().getPlayerManager();
        players.broadcast(message.formatted(Formatting.RED), false);
    }

    private static int proposeGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        return proposeGame(context.getSource(), gameSpace);
    }

    private static int proposeCurrentGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrThrow());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        return proposeGame(source, gameSpace);
    }

    private static int proposeGame(ServerCommandSource source, GameSpace gameSpace) {
        var message = GameTexts.Broadcast.propose(source, gameSpace);

        var playerManager = source.getServer().getPlayerManager();
        playerManager.broadcast(message, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinGame(CommandContext<ServerCommandSource> context, JoinIntent intent) throws CommandSyntaxException {
        new GameJoinUi(context.getSource().getPlayerOrThrow(), intent).open();
        return Command.SINGLE_SUCCESS;
    }

    private static int joinQualifiedGame(CommandContext<ServerCommandSource> context, JoinIntent intent) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        tryJoinGame(context.getSource().getPlayerOrThrow(), gameSpace, intent);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinAllGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GameSpace gameSpace = null;

        var player = context.getSource().getPlayer();
        if (player != null) {
            gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        }

        if (gameSpace == null) {
            gameSpace = getJoinableGameSpace();
        }

        joinAllPlayersToGame(context.getSource(), gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinAllQualifiedGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        joinAllPlayersToGame(context.getSource(), gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static void joinAllPlayersToGame(ServerCommandSource source, GameSpace gameSpace) {
        var playerManager = source.getServer().getPlayerManager();

        var players = playerManager.getPlayerList().stream()
                .filter(player -> !GameSpaceManagerImpl.get().inGame(player))
                .collect(Collectors.toList());

        var intent = JoinIntent.PLAY;
        var result = gameSpace.getPlayers().offer(players, intent);
        if (result.isError()) {
            source.sendError(result.errorCopy().formatted(Formatting.RED));
        }
    }

    private static void tryJoinGame(ServerPlayerEntity player, GameSpace gameSpace, JoinIntent intent) {
        var result = GamePlayerJoiner.tryJoin(player, gameSpace, intent);
        if (result.isError()) {
            player.sendMessage(result.errorCopy().formatted(Formatting.RED));
        }
    }

    private static GameSpace getJoinableGameSpace() throws CommandSyntaxException {
        return GameSpaceManagerImpl.get().getOpenGameSpaces().stream()
                .max(Comparator.comparingInt(space -> space.getPlayers().size()))
                .orElseThrow(NO_GAME_OPEN::create);
    }

    private static int locatePlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = EntityArgumentType.getPlayer(context, "player");

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace == null) {
            throw PLAYER_NOT_IN_GAME.create(player.getName());
        }

        context.getSource().sendFeedback(() -> GameTexts.Command.located(player, gameSpace), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int leaveGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrThrow();

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        Scheduler.INSTANCE.submit(server -> {
            gameSpace.getPlayers().kick(player);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrThrow());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        var startResult = gameSpace.requestStart();

        Text message;
        if (startResult.isOk()) {
            message = GameTexts.Start.startedBy(source).formatted(Formatting.GRAY);
        } else {
            message = startResult.errorCopy().formatted(Formatting.RED);
        }

        gameSpace.getPlayers().sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrThrow());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        var playerSet = gameSpace.getPlayers();

        if (playerSet.size() <= 1) {
            stopGameConfirmed(context);
        } else {
            source.sendFeedback(
                    () -> GameTexts.Stop.confirmStop().formatted(Formatting.GOLD),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGameConfirmed(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrThrow());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        var playerSet = gameSpace.getPlayers().copy(source.getServer());

        try {
            gameSpace.close(GameCloseReason.CANCELED);

            var message = GameTexts.Stop.stoppedBy(source);
            playerSet.sendMessage(message.formatted(Formatting.GRAY));
        } catch (Throwable throwable) {
            Plasmid.LOGGER.error("Failed to stop game", throwable);

            playerSet.sendMessage(GameTexts.Stop.genericError().formatted(Formatting.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listGames(CommandContext<ServerCommandSource> context) {
        var registry = context.getSource().getRegistryManager().getOrThrow(GameConfigs.REGISTRY_KEY);
        var source = context.getSource();
        source.sendFeedback(() -> GameTexts.Command.gameList().formatted(Formatting.BOLD), false);

        registry.streamEntries().forEach(game -> {
            var id = game.registryKey().getValue();
            source.sendFeedback(() -> {
                String command = "/game open " + id;

                var link = GameConfig.name(game).copy()
                        .setStyle(GameTexts.commandLinkStyle(command));

                return GameTexts.Command.listEntry(link);
            }, false);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int kickPlayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var playerManager = source.getServer().getPlayerManager();

        var targets = EntityArgumentType.getPlayers(context, "targets");

        int successes = 0;

        for (var target : targets) {
            var gameSpace = GameSpaceManagerImpl.get().byPlayer(target);
            if (gameSpace != null) {
                var message = GameTexts.Kick.kick(source, target).formatted(Formatting.GRAY);
                playerManager.broadcast(message, false);

                Scheduler.INSTANCE.submit(server -> {
                    gameSpace.getPlayers().kick(target);
                });

                successes += 1;
            }
        }

        return successes;
    }
}
