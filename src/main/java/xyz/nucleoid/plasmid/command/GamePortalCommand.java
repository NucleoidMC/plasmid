package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.command.argument.GamePortalArgument;
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
        var portal = GamePortalArgument.get(context, "portal");

        var entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface portalInterface) {
            if (!portal.addInterface(portalInterface)) {
                throw INTERFACE_ALREADY_CONNECTED.create();
            }

            var message = new TranslatableText("text.plasmid.game.portal.connect.entity", portal.getId(), entity.getEntityName());
            context.getSource().sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int connectBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var world = source.getWorld();

        var portal = GamePortalArgument.get(context, "portal");
        var pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");

        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GamePortalInterface portalInterface) {
            if (!portal.addInterface(portalInterface)) {
                throw INTERFACE_ALREADY_CONNECTED.create();
            }

            var message = new TranslatableText("text.plasmid.game.portal.connect.block", portal.getId(), pos.getX(), pos.getY(), pos.getZ());
            source.sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int disconnectEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface portalInterface) {
            portalInterface.invalidatePortal();

            var message = new TranslatableText("text.plasmid.game.portal.disconnect.entity", entity.getEntityName());
            context.getSource().sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int disconnectBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var world = source.getWorld();

        var pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");

        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GamePortalInterface portalInterface) {
            portalInterface.invalidatePortal();

            var message = new TranslatableText("text.plasmid.game.portal.disconnect.block", pos.getX(), pos.getY(), pos.getZ());
            source.sendFeedback(message.formatted(Formatting.GRAY), false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }
}
