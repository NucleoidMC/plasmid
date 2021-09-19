package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.command.argument.GameConfigArgument;
import xyz.nucleoid.plasmid.command.argument.GameSpaceArgument;
import xyz.nucleoid.plasmid.command.ui.GameJoinUi;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameTexts;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.util.Scheduler;

import java.util.Comparator;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameCommand {
    public static final SimpleCommandExceptionType NO_GAME_OPEN = new SimpleCommandExceptionType(
            new TranslatableText("text.plasmid.game.join.no_game_open")
    );

    public static final SimpleCommandExceptionType NOT_IN_GAME = new SimpleCommandExceptionType(
            new TranslatableText("text.plasmid.game.not_in_game")
    );

    public static final DynamicCommandExceptionType MALFORMED_CONFIG = new DynamicCommandExceptionType(error -> {
        return new TranslatableText("text.plasmid.game.open.malformed_config", error);
    });

    public static final DynamicCommandExceptionType PLAYER_NOT_IN_GAME = new DynamicCommandExceptionType(player -> {
        return new TranslatableText("text.plasmid.game.locate.player_not_in_game", player);
    });

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(GameConfigArgument.argument("game_config")
                        .executes(GameCommand::openGame)
                    )
                    .then(argument("game_config_nbt", NbtCompoundArgumentType.nbtCompound())
                        .executes(GameCommand::openAnonymousGame)
                    )
                )
                .then(literal("propose")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(GameSpaceArgument.argument("game_space")
                        .executes(GameCommand::proposeGame)
                    )
                        .executes(GameCommand::proposeCurrentGame)
                )
                .then(literal("start")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(GameCommand::startGame)
                )
                .then(literal("stop")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(GameCommand::stopGame)
                        .then(literal("confirm")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(GameCommand::stopGameConfirmed)
                        )
                )
                .then(literal("join")
                    .executes(GameCommand::joinGame)
                    .then(GameSpaceArgument.argument("game_space")
                        .executes(GameCommand::joinQualifiedGame)
                    )
                )
                .then(literal("joinall")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(GameCommand::joinAllGame)
                    .then(GameConfigArgument.argument("game_config")
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
        try {
            var game = GameConfigArgument.get(context, "game_config");
            return openGame(context, game.getSecond());
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFeedback(new TranslatableText("text.plasmid.game.open.error").formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int openAnonymousGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            var configNbt = NbtCompoundArgumentType.getNbtCompound(context, "game_config_nbt");
            var result = GameConfig.CODEC.parse(NbtOps.INSTANCE, configNbt);
            if (result.error().isPresent()) {
                throw MALFORMED_CONFIG.create(result.error().get());
            }

            var game = result.result().get();
            return openGame(context, game);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFeedback(new TranslatableText("text.plasmid.game.open.error").formatted(Formatting.RED), false);
            return 0;
        }
    }

    private static int openGame(CommandContext<ServerCommandSource> context, GameConfig<?> config) {
        var source = context.getSource();
        var server = source.getServer();

        var entity = source.getEntity();
        var player = entity instanceof ServerPlayerEntity ? (ServerPlayerEntity) entity : null;

        server.submit(() -> {
            if (player != null) {
                var currentGameSpace = GameSpaceManager.get().byPlayer(player);
                if (currentGameSpace != null) {
                    currentGameSpace.getPlayers().kick(player);
                }
            }

            GameSpaceManager.get().open(config)
                    .handleAsync((gameSpace, throwable) -> {
                        if (throwable == null) {
                            if (player != null) {
                                tryJoinGame(player, gameSpace);
                            }
                            onOpenSuccess(source, gameSpace);
                        } else {
                            onOpenError(source, throwable);
                        }
                        return null;
                    }, server);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void onOpenSuccess(ServerCommandSource source, GameSpace gameSpace) {
        var players = source.getServer().getPlayerManager();

        var message = GameTexts.Broadcast.gameOpened(source, gameSpace);
        players.broadcastChatMessage(message, MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static void onOpenError(ServerCommandSource source, Throwable throwable) {
        Plasmid.LOGGER.error("Failed to start game", throwable);

        var gameOpenException = GameOpenException.unwrap(throwable);

        MutableText message;
        if (gameOpenException != null) {
            message = ((GameOpenException) throwable).getReason().shallowCopy();
        } else {
            message = GameTexts.Broadcast.gameOpenError();
        }

        var players = source.getServer().getPlayerManager();
        players.broadcastChatMessage(message.formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static int proposeGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        return proposeGame(context.getSource(), gameSpace);
    }

    private static int proposeCurrentGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();

        var gameSpace = GameSpaceManager.get().byPlayer(source.getPlayer());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        return proposeGame(source, gameSpace);
    }

    private static int proposeGame(ServerCommandSource source, GameSpace gameSpace) {
        var message = GameTexts.Broadcast.propose(source, gameSpace);

        var playerManager = source.getServer().getPlayerManager();
        playerManager.broadcastChatMessage(message, MessageType.SYSTEM, Util.NIL_UUID);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        new GameJoinUi(context.getSource().getPlayer()).open();
        return Command.SINGLE_SUCCESS;
    }

    private static int joinQualifiedGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        tryJoinGame(context.getSource().getPlayer(), gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinAllGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GameSpace gameSpace = null;

        var entity = context.getSource().getEntity();
        if (entity instanceof ServerPlayerEntity) {
            gameSpace = GameSpaceManager.get().byPlayer((PlayerEntity) entity);
        }

        if (gameSpace == null) {
            gameSpace = getJoinableGameSpace();
        }

        joinAllPlayersToGame(context, gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinAllQualifiedGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        joinAllPlayersToGame(context, gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static void joinAllPlayersToGame(CommandContext<ServerCommandSource> context, GameSpace gameSpace) {
        var playerManager = context.getSource().getServer().getPlayerManager();

        var players = playerManager.getPlayerList().stream()
                .filter(player -> !GameSpaceManager.get().inGame(player))
                .collect(Collectors.toList());

        var screen = gameSpace.getPlayers().screenJoins(players);
        if (screen.isOk()) {
            for (var player : players) {
                gameSpace.getPlayers().offer(player);
            }
        } else {
            context.getSource().sendError(screen.errorCopy().formatted(Formatting.RED));
        }
    }

    private static void tryJoinGame(ServerPlayerEntity player, GameSpace gameSpace) {
        player.server.submit(() -> {
            var joiner = new GamePlayerJoiner(gameSpace);

            var results = joiner.tryJoin(player);
            results.sendErrorsTo(player);
        });
    }

    private static GameSpace getJoinableGameSpace() throws CommandSyntaxException {
        return GameSpaceManager.get().getOpenGameSpaces().stream()
                .max(Comparator.comparingInt(space -> space.getPlayers().size()))
                .orElseThrow(NO_GAME_OPEN::create);
    }

    private static int locatePlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = EntityArgumentType.getPlayer(context, "player");

        var gameSpace = GameSpaceManager.get().byPlayer(player);
        if (gameSpace == null) {
            throw PLAYER_NOT_IN_GAME.create(player.getEntityName());
        }

        var message = GameTexts.Command.located(player, gameSpace);
        context.getSource().sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int leaveGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayer();

        var gameSpace = GameSpaceManager.get().byPlayer(player);
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

        var gameSpace = GameSpaceManager.get().byPlayer(source.getPlayer());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        source.getServer().submit(() -> {
            var startResult = gameSpace.requestStart();

            Text message;
            if (startResult.isOk()) {
                message = GameTexts.Start.startedBy(source).formatted(Formatting.GRAY);
            } else {
                message = startResult.errorCopy().formatted(Formatting.RED);
            }

            gameSpace.getPlayers().sendMessage(message);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var gameSpace = GameSpaceManager.get().byPlayer(source.getPlayer());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        var playerSet = gameSpace.getPlayers();

        if (playerSet.size() <= 1) {
            stopGameConfirmed(context);
        } else {
            source.sendFeedback(
                    GameTexts.Stop.confirmStop().formatted(Formatting.GOLD),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGameConfirmed(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var gameSpace = GameSpaceManager.get().byPlayer(source.getPlayer());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        source.getServer().submit(() -> {
            var playerSet = gameSpace.getPlayers().copy(source.getServer());

            try {
                gameSpace.close(GameCloseReason.CANCELED);

                var message = GameTexts.Stop.stoppedBy(source);
                playerSet.sendMessage(message.formatted(Formatting.GRAY));
            } catch (Throwable throwable) {
                Plasmid.LOGGER.error("Failed to stop game", throwable);

                playerSet.sendMessage(GameTexts.Stop.genericError().formatted(Formatting.RED));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int listGames(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        source.sendFeedback(GameTexts.Command.gameList().formatted(Formatting.BOLD), false);

        for (var id : GameConfigs.getKeys()) {
            String command = "/game open " + id;

            var link = GameConfigs.get(id).name().shallowCopy()
                    .setStyle(GameTexts.commandLinkStyle(command));

            source.sendFeedback(GameTexts.Command.listEntry(link), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
