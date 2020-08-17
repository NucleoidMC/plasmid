package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.player.JoinResult;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameCommand {
    public static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
        return new TranslatableText("Game config with id '%s' was not found!", arg);
    });

    public static final SimpleCommandExceptionType NO_GAME_OPEN = new SimpleCommandExceptionType(
            new LiteralText("No games are open!")
    );

    public static final SimpleCommandExceptionType NO_GAME_IN_WORLD = new SimpleCommandExceptionType(
            new LiteralText("No game is open in this world!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(argument("game_type", IdentifierArgumentType.identifier()).suggests(gameSuggestions())
                    .executes(GameCommand::openGame)
                ))
                .then(literal("start")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(GameCommand::startGame)
                )
                .then(literal("stop")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(GameCommand::stopGame)
                )
                .then(literal("join").executes(GameCommand::joinGame))
                .then(literal("list").executes(GameCommand::listGames))
        );
    }
    // @formatter:on

    private static int openGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getMinecraftServer();

        Identifier gameTypeId = IdentifierArgumentType.getIdentifier(context, "game_type");
        ConfiguredGame<?> game = GameConfigs.get(gameTypeId);
        if (game == null) {
            throw GAME_NOT_FOUND.create(gameTypeId);
        }

        PlayerManager playerManager = server.getPlayerManager();

        LiteralText announcement = new LiteralText("Game is opening! Hold tight..");
        playerManager.broadcastChatMessage(announcement.formatted(Formatting.GRAY), MessageType.SYSTEM, Util.NIL_UUID);

        server.submit(() -> {
            try {
                game.open(server).handle((v, throwable) -> {
                    if (throwable == null) {
                        onOpenSuccess(playerManager);
                    } else {
                        onOpenError(playerManager, throwable);
                    }
                    return null;
                });
            } catch (Throwable throwable) {
                onOpenError(playerManager, throwable);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void onOpenSuccess(PlayerManager playerManager) {
        String command = "/game join";

        ClickEvent joinClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent joinHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(command));
        Style joinStyle = Style.EMPTY
                .withFormatting(Formatting.UNDERLINE)
                .withColor(Formatting.BLUE)
                .withClickEvent(joinClick)
                .withHoverEvent(joinHover);

        Text openMessage = new LiteralText("Game has opened! ")
                .append(new LiteralText("Click here to join").setStyle(joinStyle));
        playerManager.broadcastChatMessage(openMessage, MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static void onOpenError(PlayerManager playerManager, Throwable throwable) {
        Plasmid.LOGGER.error("Failed to start game", throwable);

        MutableText message;
        if (throwable instanceof GameOpenException) {
            message = ((GameOpenException) throwable).getReason().shallowCopy();
        } else {
            message = new LiteralText("The game threw an unexpected error while starting!");
        }

        playerManager.broadcastChatMessage(message.formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static int joinGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        MinecraftServer server = source.getMinecraftServer();

        // TODO: currently, only allowing to join one open game at a time
        Collection<GameWorld> games = GameWorld.getOpen();
        GameWorld gameWorld = games.stream().findFirst().orElse(null);
        if (gameWorld == null) {
            throw NO_GAME_OPEN.create();
        }

        CompletableFuture<JoinResult> resultFuture = CompletableFuture.supplyAsync(() -> gameWorld.offerPlayer(player), server);

        resultFuture.thenAccept(joinResult -> {
            if (joinResult.isError()) {
                Text error = joinResult.getError();
                source.sendError(error.shallowCopy().formatted(Formatting.RED));
                return;
            }

            Text joinMessage = player.getDisplayName().shallowCopy()
                    .append(" has joined the game lobby!")
                    .formatted(Formatting.YELLOW);

            for (ServerPlayerEntity otherPlayer : gameWorld.getPlayers()) {
                otherPlayer.sendMessage(joinMessage, false);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getMinecraftServer();

        GameWorld gameWorld = GameWorld.forWorld(source.getWorld());
        if (gameWorld == null) {
            throw NO_GAME_IN_WORLD.create();
        }

        CompletableFuture<StartResult> resultFuture = CompletableFuture.supplyAsync(gameWorld::requestStart, server);

        resultFuture.thenAccept(startResult -> {
            if (startResult.isError()) {
                Text error = startResult.getError();
                source.sendError(error.shallowCopy().formatted(Formatting.RED));
            }

            Text message = new LiteralText("Game is starting!").formatted(Formatting.GRAY);
            for (ServerPlayerEntity otherPlayer : gameWorld.getPlayers()) {
                otherPlayer.sendMessage(message, false);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        GameWorld gameWorld = GameWorld.forWorld(source.getWorld());
        if (gameWorld == null) {
            throw NO_GAME_IN_WORLD.create();
        }

        MinecraftServer server = source.getMinecraftServer();
        PlayerManager playerManager = server.getPlayerManager();

        try {
            gameWorld.close();

            LiteralText message = new LiteralText("Game has been stopped");
            playerManager.broadcastChatMessage(message.formatted(Formatting.GRAY), MessageType.SYSTEM, Util.NIL_UUID);
        } catch (Throwable throwable) {
            Plasmid.LOGGER.error("Failed to stop game", throwable);

            LiteralText message = new LiteralText("An unexpected error was thrown while stopping the game!");
            playerManager.broadcastChatMessage(message.formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listGames(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText("Registered games:").formatted(Formatting.BOLD), false);
        for (Identifier id : GameConfigs.getKeys()) {
            String command = "/game open " + id;

            ClickEvent linkClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
            HoverEvent linkHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(command));
            Style linkStyle = Style.EMPTY
                    .withFormatting(Formatting.UNDERLINE)
                    .withColor(Formatting.BLUE)
                    .withClickEvent(linkClick)
                    .withHoverEvent(linkHover);

            MutableText link = new LiteralText(id.toString()).setStyle(linkStyle);
            source.sendFeedback(new LiteralText(" - ").append(link), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static SuggestionProvider<ServerCommandSource> gameSuggestions() {
        return (ctx, builder) -> {
            return CommandSource.suggestMatching(
                    GameConfigs.getKeys().stream().map(Identifier::toString),
                    builder
            );
        };
    }
}
