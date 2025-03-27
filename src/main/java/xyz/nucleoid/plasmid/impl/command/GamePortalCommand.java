package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.impl.command.argument.GamePortalArgument;
import xyz.nucleoid.plasmid.impl.portal.GamePortalInterface;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GamePortalCommand {
    public static final SimpleCommandExceptionType TARGET_IS_NOT_INTERFACE = new SimpleCommandExceptionType(
            Text.translatable("text.plasmid.game.portal.connect.target_is_not_interface")
    );

    public static final SimpleCommandExceptionType INTERFACE_ALREADY_CONNECTED = new SimpleCommandExceptionType(
            Text.translatable("text.plasmid.game.portal.connect.interface_already_connected")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("portal")
                    .then(literal("connect")
                        .requires(Permissions.require("plasmid.command.game.portal.connect", 3))
                        .then(GamePortalArgument.argument("portal")
                        .then(argument("entity", EntityArgumentType.entity()).executes(GamePortalCommand::connectEntity))
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(GamePortalCommand::connectBlock))
                    ))
                    .then(literal("disconnect")
                        .requires(Permissions.require("plasmid.command.game.portal.disconnect", 3))
                        .then(argument("entity", EntityArgumentType.entity()).executes(GamePortalCommand::disconnectEntity))
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(GamePortalCommand::disconnectBlock))
                    )
                    .then(literal("open")
                        .then(GamePortalArgument.argument("portal").executes(GamePortalCommand::openPortal))
                    )
                )
        );
    }
    // @formatter:on

    private static int openPortal(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var portal = GamePortalArgument.get(context, "portal");
        portal.requestJoin(context.getSource().getPlayer(), false);
        return 1;
    }

    private static int connectEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var portal = GamePortalArgument.get(context, "portal");

        var entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface portalInterface) {
            if (!portal.addInterface(portalInterface)) {
                throw INTERFACE_ALREADY_CONNECTED.create();
            }

            context.getSource().sendFeedback(() -> {
                var message = Text.translatable("text.plasmid.game.portal.connect.entity", Text.of(portal.getId()), entity.getName());
                return message.formatted(Formatting.GRAY);
            }, false);

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

            source.sendFeedback(() -> {
                var message = Text.translatable("text.plasmid.game.portal.connect.block", Text.of(portal.getId()), pos.getX(), pos.getY(), pos.getZ());
                return message.formatted(Formatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int disconnectEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var entity = EntityArgumentType.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface portalInterface) {
            portalInterface.invalidatePortal();

            context.getSource().sendFeedback(() -> {
                var message = Text.translatable("text.plasmid.game.portal.disconnect.entity", entity.getName());
                return message.formatted(Formatting.GRAY);
            }, false);

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

            source.sendFeedback(() -> {
                var message = Text.translatable("text.plasmid.game.portal.disconnect.block", pos.getX(), pos.getY(), pos.getZ());
                return message.formatted(Formatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }
}
