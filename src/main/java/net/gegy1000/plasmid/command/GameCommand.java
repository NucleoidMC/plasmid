package net.gegy1000.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.gegy1000.plasmid.Plasmid;
import net.gegy1000.plasmid.game.ConfiguredGame;
import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.GameWorldState;
import net.gegy1000.plasmid.game.StartResult;
import net.gegy1000.plasmid.game.config.GameConfigs;
import net.gegy1000.plasmid.game.player.JoinResult;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameCommand {
    public static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
        return new TranslatableText("Game with id '%s' was not found!", arg);
    });

    public static final SimpleCommandExceptionType ALREADY_OPEN = new SimpleCommandExceptionType(
            new LiteralText("A game is already open!")
    );

    public static final SimpleCommandExceptionType NOT_RECRUITING = new SimpleCommandExceptionType(
            new LiteralText("Game not recruiting!")
    );

    public static final SimpleCommandExceptionType UNSUPPORTED_WORLD = new SimpleCommandExceptionType(
            new LiteralText("This dimension does not support running games!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(argument("game_type", IdentifierArgumentType.identifier()).suggests(gameSuggestions())
                    .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(GameCommand::openGame)
                )))
                .then(literal("start")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(GameCommand::startGame)
                ))
                .then(literal("stop")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(GameCommand::stopGame)
                ))
                .then(literal("join")
                    .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(GameCommand::joinGame)
                ))
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

        GameWorldState gameState = getGameState(context, source);
        if (gameState.isOpen()) {
            throw ALREADY_OPEN.create();
        }

        PlayerManager playerManager = server.getPlayerManager();

        LiteralText announcement = new LiteralText("Game is opening! Hold tight..");
        playerManager.broadcastChatMessage(announcement.formatted(Formatting.GRAY), MessageType.SYSTEM, Util.NIL_UUID);

        game.open(gameState).handle((v, throwable) -> {
            if (throwable == null) {
                onOpenSuccess(playerManager, gameState.getDimension());
            } else {
                onOpenError(playerManager, throwable);
            }
            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void onOpenSuccess(PlayerManager playerManager, RegistryKey<World> dimension) {
        String command = "/game join " + dimension.getValue();

        ClickEvent joinClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent joinHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(command));
        Style joinStyle = Style.EMPTY
                .withFormatting(Formatting.UNDERLINE)
                .withColor(Formatting.BLUE)
                .withClickEvent(joinClick)
                .setHoverEvent(joinHover);

        Text openMessage = new LiteralText("Game has opened! ")
                .append(new LiteralText("Click here to join").setStyle(joinStyle));
        playerManager.broadcastChatMessage(openMessage, MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static void onOpenError(PlayerManager playerManager, Throwable throwable) {
        Plasmid.LOGGER.error("Failed to start game", throwable);
        playerManager.broadcastChatMessage(new LiteralText("An exception occurred while trying to start game"), MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static int joinGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();

        GameWorldState gameState = getGameState(context, source);
        GameWorld openWorld = gameState.getOpenWorld();

        if (openWorld == null) {
            throw NOT_RECRUITING.create();
        }

        JoinResult joinResult = openWorld.offerPlayer(player);
        if (joinResult.isErr()) {
            Text error = joinResult.getError();
            throw new SimpleCommandExceptionType(error).create();
        }

        Text joinMessage = player.getDisplayName().shallowCopy()
                .append(" has joined the game lobby!")
                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
        playerManager.broadcastChatMessage(joinMessage, MessageType.SYSTEM, Util.NIL_UUID);

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        GameWorldState gameState = getGameState(context, source);
        GameWorld openWorld = gameState.getOpenWorld();

        if (openWorld == null) {
            throw NOT_RECRUITING.create();
        }

        StartResult startResult = openWorld.requestStart();
        if (startResult.isErr()) {
            Text error = startResult.getError();
            throw new SimpleCommandExceptionType(error).create();
        }

        MinecraftServer server = source.getMinecraftServer();
        PlayerManager playerManager = server.getPlayerManager();
        playerManager.broadcastChatMessage(new LiteralText("Game is starting!"), MessageType.SYSTEM, Util.NIL_UUID);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GameWorldState gameState = getGameState(context, context.getSource());
        GameWorld openWorld = gameState.getOpenWorld();

        if (openWorld == null) {
            throw NOT_RECRUITING.create();
        }

        openWorld.closeWorld();

        MinecraftServer server = context.getSource().getMinecraftServer();

        LiteralText message = new LiteralText("Game has been stopped");
        server.getPlayerManager().broadcastChatMessage(message, MessageType.SYSTEM, Util.NIL_UUID);

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
                    .setHoverEvent(linkHover);

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

    private static GameWorldState getGameState(CommandContext<ServerCommandSource> context, ServerCommandSource source) throws CommandSyntaxException {
        World dimension = DimensionArgumentType.getDimensionArgument(context, "dimension");

        GameWorldState gameWorld = GameWorldState.forWorld(dimension);
        if (gameWorld == null) {
            throw UNSUPPORTED_WORLD.create();
        }

        return gameWorld;
    }
}
