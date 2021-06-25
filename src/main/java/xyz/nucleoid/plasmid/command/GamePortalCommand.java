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
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.command.argument.GamePortalArgument;
import xyz.nucleoid.plasmid.game.portal.GamePortal;
import xyz.nucleoid.plasmid.game.portal.GamePortalInterface;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GamePortalCommand {
    public static final SimpleCommandExceptionType TARGET_IS_NOT_INTERFACE = new SimpleCommandExceptionType(
            new TranslatableText("text.plasmid.game.portal.connect.target_is_not_interface")
    );

    public static final SimpleCommandExceptionType INTERFACE_ALREADY_CONNECTED = new SimpleCommandExceptionType(
            new TranslatableText("text.plasmid.game.portal.connect.interface_already_connected")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("portal")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("connect")
                        .then(GamePortalArgument.argument("portal")
                        .then(argument("entity", EntityArgumentType.entity()).executes(GamePortalCommand::connectEntity))
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(GamePortalCommand::connectBlock))
                    ))
                    .then(literal("disconnect")
                        .then(argument("entity", EntityArgumentType.entity()).executes(GamePortalCommand::disconnectEntity))
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(GamePortalCommand::disconnectBlock))
                    )
                )
        );
    }
    // @formatter:on

    private static int connectEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GamePortal portal = GamePortalArgument.get(context, "portal");

        Entity entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface) {
            if (!portal.addInterface((GamePortalInterface) entity)) {
                throw INTERFACE_ALREADY_CONNECTED.create();
            }

            MutableText message = new TranslatableText("text.plasmid.game.portal.connect.entity", portal.getId(), entity.getEntityName());
            context.getSource().sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int connectBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        GamePortal portal = GamePortalArgument.get(context, "portal");
        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GamePortalInterface) {
            if (!portal.addInterface((GamePortalInterface) blockEntity)) {
                throw INTERFACE_ALREADY_CONNECTED.create();
            }

            MutableText message = new TranslatableText("text.plasmid.game.portal.connect.block", portal.getId(), pos.getX(), pos.getY(), pos.getZ());
            source.sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int disconnectEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Entity entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface) {
            ((GamePortalInterface) entity).invalidatePortal();

            MutableText message = new TranslatableText("text.plasmid.game.portal.disconnect.entity", entity.getEntityName());
            context.getSource().sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int disconnectBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GamePortalInterface) {
            ((GamePortalInterface) blockEntity).invalidatePortal();

            MutableText message = new TranslatableText("text.plasmid.game.portal.disconnect.block", pos.getX(), pos.getY(), pos.getZ());
            source.sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }
}
