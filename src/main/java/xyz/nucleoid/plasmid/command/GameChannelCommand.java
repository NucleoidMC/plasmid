package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.command.argument.GameChannelArgument;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.GameChannelInterface;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameChannelCommand {
    public static final SimpleCommandExceptionType TARGET_IS_NOT_UI = new SimpleCommandExceptionType(
            new LiteralText("The selected target is not a valid game channel user interface!")
    );

    public static final SimpleCommandExceptionType UI_ALREADY_CONNECTED = new SimpleCommandExceptionType(
            new LiteralText("The selected user interface is already connected to this channel!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("channel")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("connect")
                        .then(GameChannelArgument.argument("channel")
                        .then(argument("entity", EntityArgumentType.entity()).executes(GameChannelCommand::connectEntityToChannel))
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(GameChannelCommand::connectBlockToChannel))
                    ))
                )
        );
    }
    // @formatter:on

    private static int connectEntityToChannel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GameChannel channel = GameChannelArgument.get(context, "channel");

        Entity entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof GameChannelInterface) {
            if (!channel.addInterface((GameChannelInterface) entity)) {
                throw UI_ALREADY_CONNECTED.create();
            }

            MutableText message = new TranslatableText("text.plasmid.game.channel.connect.entity", channel.getId(), entity.getEntityName());
            context.getSource().sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_UI.create();
        }
    }

    private static int connectBlockToChannel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        GameChannel channel = GameChannelArgument.get(context, "channel");
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GameChannelInterface) {
            if (!channel.addInterface((GameChannelInterface) blockEntity)) {
                throw UI_ALREADY_CONNECTED.create();
            }

            MutableText message = new TranslatableText("text.plasmid.game.channel.connect.block", channel.getId(), pos.getX(), pos.getY(), pos.getZ());
            source.sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_UI.create();
        }
    }
}
