package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import org.slf4j.Logger;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.impl.Plasmid;
import xyz.nucleoid.plasmid.impl.command.argument.GameConfigArgument;
import xyz.nucleoid.plasmid.impl.command.argument.GameSpaceArgument;
import xyz.nucleoid.plasmid.impl.command.ui.GameJoinUi;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.GameComponents;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;
import xyz.nucleoid.plasmid.api.game.player.GamePlayerJoiner;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.util.Scheduler;

import java.util.Comparator;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static xyz.nucleoid.plasmid.impl.Plasmid.id;

public final class GameCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SimpleCommandExceptionType NO_GAME_OPEN = new SimpleCommandExceptionType(
            Component.translatable("text.plasmid.game.join.no_game_open")
    );

    public static final SimpleCommandExceptionType NOT_IN_GAME = new SimpleCommandExceptionType(
            Component.translatable("text.plasmid.game.not_in_game")
    );

    public static final DynamicCommandExceptionType MALFORMED_CONFIG = new DynamicCommandExceptionType(error ->
            Component.translatableEscape("text.plasmid.game.open.malformed_config", error)
    );

    public static final DynamicCommandExceptionType PLAYER_NOT_IN_GAME = new DynamicCommandExceptionType(player ->
            Component.translatableEscape("text.plasmid.game.locate.player_not_in_game", player)
    );

    // @formatter:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .requires(PermissionPredicates.require(id("command/game/open"), PermissionLevel.GAMEMASTERS))
                    .then(GameConfigArgument.argument("game_config")
                        .executes(GameCommand::openGame)
                    )
                    .then(argument("game_config_nbt", CompoundTagArgument.compoundTag())
                        .executes(GameCommand::openAnonymousGame)
                    )
                )
                .then(literal("propose")
                    .requires(PermissionPredicates.require(id("command/game/propose"), PermissionLevel.GAMEMASTERS))
                    .then(GameSpaceArgument.argument("game_space")
                        .executes(GameCommand::proposeGame)
                    )
                        .executes(GameCommand::proposeCurrentGame)
                )
                .then(literal("start")
                    .requires(PermissionPredicates.require(id("command/game/start"), PermissionLevel.GAMEMASTERS))
                    .executes(GameCommand::startGame)
                )
                .then(literal("stop")
                    .requires(PermissionPredicates.require(id("command/game/stop"), PermissionLevel.GAMEMASTERS))
                    .executes(GameCommand::stopGame)
                        .then(literal("confirm")
                            .executes(GameCommand::stopGameConfirmed)
                        )
                )
                .then(literal("kick")
                    .requires(PermissionPredicates.require(id("command/game/kick"), PermissionLevel.GAMEMASTERS))
                    .then(argument("targets", EntityArgument.players())
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
                    .requires(PermissionPredicates.require(id("command/game.joinall"), PermissionLevel.GAMEMASTERS))
                    .executes(GameCommand::joinAllGame)
                    .then(GameSpaceArgument.argument("game_space")
                        .executes(GameCommand::joinAllQualifiedGame)
                    )
                )
                .then(literal("locate")
                        .then(argument("player", EntityArgument.player())
                        .executes(GameCommand::locatePlayer))
                )
                .then(literal("leave").executes(GameCommand::leaveGame))
                .then(literal("list").executes(GameCommand::listGames))
        );
    }
    // @formatter:on

    private static int openGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return openGame(context, false);
    }

    protected static int openGame(CommandContext<CommandSourceStack> context, boolean test) throws CommandSyntaxException {
        try {
            var game = GameConfigArgument.get(context, "game_config");
            return openGame(context, game, test);
        } catch (CommandSyntaxException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while opening a game", e);
            context.getSource().sendSuccess(() -> Component.translatable("text.plasmid.game.open.error").withStyle(ChatFormatting.RED), false);
            return 0;
        }
    }

    private static int openAnonymousGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return openAnonymousGame(context, false);
    }

    protected static int openAnonymousGame(CommandContext<CommandSourceStack> context, boolean test) throws CommandSyntaxException {
        try {
            var configNbt = CompoundTagArgument.getCompoundTag(context, "game_config_nbt");
            var game = GameConfig.DIRECT_CODEC.parse(context.getSource().registryAccess().createSerializationContext(NbtOps.INSTANCE), configNbt)
                    .getOrThrow(MALFORMED_CONFIG::create);
            return openGame(context, Holder.direct(game), test);
        } catch (CommandSyntaxException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while opening a game", e);
            context.getSource().sendSuccess(() -> Component.translatable("text.plasmid.game.open.error").withStyle(ChatFormatting.RED), false);
            return 0;
        }
    }

    private static int openGame(CommandContext<CommandSourceStack> context, Holder<GameConfig<?>> config, boolean test) {
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

    private static void onOpenSuccess(CommandSourceStack source, GameSpace gameSpace, ServerPlayer player, boolean test) {
        var players = source.getServer().getPlayerList();

        var message = test ? GameComponents.Broadcast.gameOpenedTesting(source, gameSpace) : GameComponents.Broadcast.gameOpened(source, gameSpace);
        if (test) {
            players.broadcastSystemMessage(message, false);
            joinAllPlayersToGame(source, gameSpace);

            var startResult = gameSpace.requestStart();

            if (!startResult.isOk()) {
                var error = startResult.errorCopy().withStyle(ChatFormatting.RED);
                gameSpace.getPlayers().sendMessage(error);
            }
        } else if (player != null) {
            tryJoinGame(player, gameSpace, JoinIntent.PLAY);
            // only send messages to players that are allowed to join
            players.getPlayers().stream().
                    filter((plr) -> gameSpace.isPlayerAllowed(PlayerRef.of(plr)))
                    .forEach((plr -> plr.sendSystemMessage(message, false)));
        }
    }

    private static void onOpenError(CommandSourceStack source, Throwable throwable) {
        Plasmid.LOGGER.error("Failed to start game", throwable);

        var gameOpenException = GameOpenException.unwrap(throwable);

        MutableComponent message;
        if (gameOpenException != null) {
            message = gameOpenException.getReason().copy();
        } else {
            message = GameComponents.Broadcast.gameOpenError();
        }

        var players = source.getServer().getPlayerList();
        players.broadcastSystemMessage(message.withStyle(ChatFormatting.RED), false);
    }

    private static int proposeGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        return proposeGame(context.getSource(), gameSpace);
    }

    private static int proposeCurrentGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrException());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        return proposeGame(source, gameSpace);
    }

    private static int proposeGame(CommandSourceStack source, GameSpace gameSpace) {
        var message = GameComponents.Broadcast.propose(source, gameSpace);

        var playerManager = source.getServer().getPlayerList();
        playerManager.broadcastSystemMessage(message, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinGame(CommandContext<CommandSourceStack> context, JoinIntent intent) throws CommandSyntaxException {
        new GameJoinUi(context.getSource().getPlayerOrException(), intent).open();
        return Command.SINGLE_SUCCESS;
    }

    private static int joinQualifiedGame(CommandContext<CommandSourceStack> context, JoinIntent intent) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        tryJoinGame(context.getSource().getPlayerOrException(), gameSpace, intent);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinAllGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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

    private static int joinAllQualifiedGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var gameSpace = GameSpaceArgument.get(context, "game_space");
        joinAllPlayersToGame(context.getSource(), gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static void joinAllPlayersToGame(CommandSourceStack source, GameSpace gameSpace) {
        var playerManager = source.getServer().getPlayerList();

        var players = playerManager.getPlayers().stream()
                .filter(player -> !GameSpaceManagerImpl.get().inGame(player))
                .collect(Collectors.toList());

        var intent = JoinIntent.PLAY;
        var result = gameSpace.getPlayers().offer(players, intent);
        if (result.isError()) {
            source.sendFailure(result.errorCopy().withStyle(ChatFormatting.RED));
        }
    }

    private static void tryJoinGame(ServerPlayer player, GameSpace gameSpace, JoinIntent intent) {
        var result = GamePlayerJoiner.tryJoin(player, gameSpace, intent);
        if (result.isError()) {
            player.sendSystemMessage(result.errorCopy().withStyle(ChatFormatting.RED));
        }
    }

    private static GameSpace getJoinableGameSpace() throws CommandSyntaxException {
        return GameSpaceManagerImpl.get().getOpenGameSpaces().stream()
                .max(Comparator.comparingInt(space -> space.getPlayers().size()))
                .orElseThrow(NO_GAME_OPEN::create);
    }

    private static int locatePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace == null) {
            throw PLAYER_NOT_IN_GAME.create(player.getName());
        }

        context.getSource().sendSuccess(() -> GameComponents.Command.located(player, gameSpace), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int leaveGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(player);
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        Scheduler.INSTANCE.submit(server -> {
            gameSpace.getPlayers().kick(player);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();

        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrException());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        var startResult = gameSpace.requestStart();

        Component message;
        if (startResult.isOk()) {
            message = GameComponents.Start.startedBy(source).withStyle(ChatFormatting.GRAY);
        } else {
            message = startResult.errorCopy().withStyle(ChatFormatting.RED);
        }

        gameSpace.getPlayers().sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrException());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        var playerSet = gameSpace.getPlayers();

        if (playerSet.size() <= 1) {
            stopGameConfirmed(context);
        } else {
            source.sendSuccess(
                    () -> GameComponents.Stop.confirmStop().withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGameConfirmed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var gameSpace = GameSpaceManagerImpl.get().byPlayer(source.getPlayerOrException());
        if (gameSpace == null) {
            throw NOT_IN_GAME.create();
        }

        var playerSet = gameSpace.getPlayers().copy(source.getServer());

        try {
            gameSpace.close(GameCloseReason.CANCELED);

            var message = GameComponents.Stop.stoppedBy(source);
            playerSet.sendMessage(message.withStyle(ChatFormatting.GRAY));
        } catch (Throwable throwable) {
            Plasmid.LOGGER.error("Failed to stop game", throwable);

            playerSet.sendMessage(GameComponents.Stop.genericError().withStyle(ChatFormatting.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listGames(CommandContext<CommandSourceStack> context) {
        var registry = context.getSource().registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_CONFIG);
        var source = context.getSource();
        source.sendSuccess(() -> GameComponents.Command.gameList().withStyle(ChatFormatting.BOLD), false);

        registry.listElements().forEach(game -> {
            var id = game.key().identifier();
            source.sendSuccess(() -> {
                String command = "/game open " + id;

                var link = GameConfig.name(game).copy()
                        .setStyle(GameComponents.commandLinkStyle(command));

                return GameComponents.Command.listEntry(link);
            }, false);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int kickPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var playerManager = source.getServer().getPlayerList();

        var targets = EntityArgument.getPlayers(context, "targets");

        int successes = 0;

        for (var target : targets) {
            var gameSpace = GameSpaceManagerImpl.get().byPlayer(target);
            if (gameSpace != null) {
                var message = GameComponents.Kick.kick(source, target).withStyle(ChatFormatting.GRAY);
                playerManager.broadcastSystemMessage(message, false);

                Scheduler.INSTANCE.submit(server -> {
                    gameSpace.getPlayers().kick(target);
                });

                successes += 1;
            }
        }

        return successes;
    }
}
