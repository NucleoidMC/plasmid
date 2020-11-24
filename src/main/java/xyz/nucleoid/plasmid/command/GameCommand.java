package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.command.argument.GameChannelArgument;
import xyz.nucleoid.plasmid.command.argument.GameConfigArgument;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.channel.ChannelEndpoint;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.GameChannelManager;
import xyz.nucleoid.plasmid.game.channel.SimpleGameChannel;
import xyz.nucleoid.plasmid.game.config.GameConfigs;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.util.Scheduler;

import java.util.Collection;
import java.util.Comparator;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameCommand {
    public static final SimpleCommandExceptionType NO_GAME_OPEN = new SimpleCommandExceptionType(
            new LiteralText("No games are open!")
    );

    public static final SimpleCommandExceptionType NO_GAME_IN_WORLD = new SimpleCommandExceptionType(
            new LiteralText("No game is open in this world!")
    );

    public static final DynamicCommandExceptionType CHANNEL_ALREADY_EXISTS = new DynamicCommandExceptionType(id -> {
        return new TranslatableText("Channel with id '%s' already exists!", id);
    });

    public static final SimpleCommandExceptionType TARGET_IS_NOT_ENDPOINT = new SimpleCommandExceptionType(
            new LiteralText("The selected target is not a valid game channel endpoint!")
    );

    public static final SimpleCommandExceptionType ENDPOINT_ALREADY_CONNECTED = new SimpleCommandExceptionType(
            new LiteralText("The selected endpoint is already connected to this channel!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(GameConfigArgument.argument("game_type")
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
                .then(literal("join")
                        .executes(GameCommand::joinGame)
                        .then(GameConfigArgument.argument("game_type")
                            .executes(GameCommand::joinQualifiedGame)
                        )
                )
                .then(literal("leave").executes(GameCommand::leaveGame))
                .then(literal("list").executes(GameCommand::listGames))
                .then(literal("channel")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("open")
                        .then(argument("channel_id", IdentifierArgumentType.identifier())
                        .then(GameConfigArgument.argument("game_type")
                        .executes(GameCommand::openChannel)
                    )))
                    .then(literal("remove")
                        .then(GameChannelArgument.argument("channel_id")
                        .executes(GameCommand::removeChannel)
                    ))
                    .then(literal("connect")
                        .then(GameChannelArgument.argument("channel_id")
                        .then(argument("entity", EntityArgumentType.entity()).executes(GameCommand::connectEntityToChannel))
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(GameCommand::connectBlockToChannel))
                    ))
                )
        );
    }
    // @formatter:on

    private static int openGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getMinecraftServer();

        Pair<Identifier, ConfiguredGame<?>> game = GameConfigArgument.get(context, "game_type");

        PlayerManager playerManager = server.getPlayerManager();
        server.submit(() -> {
            try {
                game.getRight().open(server).handle((v, throwable) -> {
                    if (throwable == null) {
                        onOpenSuccess(source, game.getLeft(), game.getRight(), playerManager);
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

    private static void onOpenSuccess(ServerCommandSource source, Identifier gameId, ConfiguredGame<?> game, PlayerManager playerManager) {
        String command = "/game join " + gameId;

        ClickEvent joinClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent joinHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(command));
        Style joinStyle = Style.EMPTY
                .withFormatting(Formatting.UNDERLINE)
                .withColor(Formatting.BLUE)
                .withClickEvent(joinClick)
                .withHoverEvent(joinHover);

        Text openMessage = new TranslatableText("text.plasmid.game.open.opened", source.getDisplayName(), new LiteralText(game.getName()).formatted(Formatting.GRAY))
                .append(new TranslatableText("text.plasmid.game.open.join").setStyle(joinStyle));
        playerManager.broadcastChatMessage(openMessage, MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static void onOpenError(PlayerManager playerManager, Throwable throwable) {
        Plasmid.LOGGER.error("Failed to start game", throwable);

        MutableText message;
        if (throwable instanceof GameOpenException) {
            message = ((GameOpenException) throwable).getReason().shallowCopy();
        } else {
            message = new TranslatableText("text.plasmid.game.open.error");
        }

        playerManager.broadcastChatMessage(message.formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static int joinGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ManagedGameSpace> games = ManagedGameSpace.getOpen();
        ManagedGameSpace gameSpace = games.stream().max(Comparator.comparingInt(ManagedGameSpace::getPlayerCount)).orElse(null);
        if (gameSpace == null) {
            throw NO_GAME_OPEN.create();
        }

        joinPlayerToGame(context, gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinQualifiedGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ConfiguredGame<?> game = GameConfigArgument.get(context, "game_type").getRight();

        Collection<ManagedGameSpace> games = ManagedGameSpace.getOpen();
        ManagedGameSpace gameSpace = games.stream()
                .filter(gw -> gw.getGameConfig() == game)
                .max(Comparator.comparingInt(ManagedGameSpace::getPlayerCount))
                .orElse(null);

        if (gameSpace == null) {
            throw NO_GAME_OPEN.create();
        }

        joinPlayerToGame(context, gameSpace);

        return Command.SINGLE_SUCCESS;
    }

    private static void joinPlayerToGame(CommandContext<ServerCommandSource> context, ManagedGameSpace gameSpace) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        gameSpace.offerPlayer(player).thenAccept(joinResult -> {
            if (joinResult.isError()) {
                Text error = joinResult.getError();
                source.sendError(error.shallowCopy().formatted(Formatting.RED));
            }
        });
    }

    private static int leaveGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(source.getWorld());
        if (gameSpace == null) {
            throw NO_GAME_IN_WORLD.create();
        }

        Scheduler.INSTANCE.submit(server -> {
            gameSpace.removePlayer(player);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(source.getWorld());
        if (gameSpace == null) {
            throw NO_GAME_IN_WORLD.create();
        }

        gameSpace.requestStart().thenAccept(startResult -> {
            Text message;
            if (startResult.isError()) {
                Text error = startResult.getError();
                message = error.shallowCopy().formatted(Formatting.RED);
            } else {
                message = new TranslatableText("text.plasmid.game.started.player", source.getDisplayName())
                        .formatted(Formatting.GRAY);
            }

            gameSpace.getPlayers().sendMessage(message);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(source.getWorld());
        if (gameSpace == null) {
            throw NO_GAME_IN_WORLD.create();
        }

        PlayerSet playerSet = gameSpace.getPlayers().copy();

        try {
            gameSpace.close();

            MutableText message = new TranslatableText("text.plasmid.game.stopped.player", source.getDisplayName());
            playerSet.sendMessage(message.formatted(Formatting.GRAY));
        } catch (Throwable throwable) {
            Plasmid.LOGGER.error("Failed to stop game", throwable);

            MutableText message = new TranslatableText("text.plasmid.game.stopped.error");
            playerSet.sendMessage(message.formatted(Formatting.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listGames(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new TranslatableText("text.plasmid.game.list").formatted(Formatting.BOLD), false);

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

    private static int openChannel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getMinecraftServer();
        GameChannelManager channelManager = GameChannelManager.get(server);
        Identifier channelId = IdentifierArgumentType.getIdentifier(context, "channel_id");
        Pair<Identifier, ConfiguredGame<?>> game = GameConfigArgument.get(context, "game_type");

        SimpleGameChannel channel = new SimpleGameChannel(channelId, game.getLeft());
        if (!channelManager.add(channel)) {
            throw CHANNEL_ALREADY_EXISTS.create(channelId);
        }

        MutableText message = new TranslatableText("text.plasmid.game.channel.create", channelId);
        source.sendFeedback(message.formatted(Formatting.GRAY), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeChannel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getMinecraftServer();
        GameChannelManager channelManager = GameChannelManager.get(server);

        Identifier channelId = GameChannelArgument.get(context, "channel_id").getLeft();
        channelManager.remove(channelId);

        MutableText message = new TranslatableText("text.plasmid.game.channel.remove", channelId);
        source.sendFeedback(message.formatted(Formatting.GRAY), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int connectEntityToChannel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Pair<Identifier, GameChannel> channel = GameChannelArgument.get(context, "channel_id");

        Entity entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof ChannelEndpoint) {
            if (!channel.getRight().connectTo((ChannelEndpoint) entity)) {
                throw ENDPOINT_ALREADY_CONNECTED.create();
            }

            MutableText message = new TranslatableText("text.plasmid.game.channel.connect.entity", channel.getLeft(), entity.getEntityName());
            context.getSource().sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_ENDPOINT.create();
        }
    }

    private static int connectBlockToChannel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        Pair<Identifier, GameChannel> channel = GameChannelArgument.get(context, "channel_id");
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChannelEndpoint) {
            if (!channel.getRight().connectTo((ChannelEndpoint) blockEntity)) {
                throw ENDPOINT_ALREADY_CONNECTED.create();
            }

            MutableText message = new TranslatableText("text.plasmid.game.channel.connect.block", channel.getLeft(), pos.getX(), pos.getY(), pos.getZ());
            source.sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_ENDPOINT.create();
        }
    }
}
