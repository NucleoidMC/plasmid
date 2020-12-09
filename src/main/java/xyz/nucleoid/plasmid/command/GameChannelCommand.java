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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.command.argument.GameChannelArgument;
import xyz.nucleoid.plasmid.command.argument.GameConfigArgument;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.channel.ChannelEndpoint;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.GameChannelManager;
import xyz.nucleoid.plasmid.game.channel.SimpleGameChannel;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameChannelCommand {
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
                .then(literal("channel")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("open")
                        .then(argument("channel_id", IdentifierArgumentType.identifier())
                        .then(GameConfigArgument.argument("game_type")
                        .executes(GameChannelCommand::openChannel)
                    )))
                    .then(literal("remove")
                        .then(GameChannelArgument.argument("channel_id")
                        .executes(GameChannelCommand::removeChannel)
                    ))
                    .then(literal("connect")
                        .then(GameChannelArgument.argument("channel_id")
                        .then(argument("entity", EntityArgumentType.entity()).executes(GameChannelCommand::connectEntityToChannel))
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(GameChannelCommand::connectBlockToChannel))
                    ))
                )
        );
    }
    // @formatter:on

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
