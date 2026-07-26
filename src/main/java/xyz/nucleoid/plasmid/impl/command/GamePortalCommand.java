package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import xyz.nucleoid.plasmid.impl.command.argument.GamePortalArgument;
import xyz.nucleoid.plasmid.impl.portal.GamePortalInterface;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static xyz.nucleoid.plasmid.impl.Plasmid.id;

public final class GamePortalCommand {
    public static final SimpleCommandExceptionType TARGET_IS_NOT_INTERFACE = new SimpleCommandExceptionType(
            Component.translatable("text.plasmid.game.portal.connect.target_is_not_interface")
    );

    public static final SimpleCommandExceptionType INTERFACE_ALREADY_CONNECTED = new SimpleCommandExceptionType(
            Component.translatable("text.plasmid.game.portal.connect.interface_already_connected")
    );

    // @formatter:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("portal")
                    .then(literal("connect")
                        .requires(PermissionPredicates.require(id("command/game/portal/connect"), PermissionLevel.ADMINS))
                        .then(GamePortalArgument.argument("portal")
                        .then(argument("entity", EntityArgument.entity()).executes(GamePortalCommand::connectEntity))
                        .then(argument("pos", BlockPosArgument.blockPos()).executes(GamePortalCommand::connectBlock))
                    ))
                    .then(literal("disconnect")
                        .requires(PermissionPredicates.require(id("command/game/portal/disconnect"), PermissionLevel.ADMINS))
                        .then(argument("entity", EntityArgument.entity()).executes(GamePortalCommand::disconnectEntity))
                        .then(argument("pos", BlockPosArgument.blockPos()).executes(GamePortalCommand::disconnectBlock))
                    )
                    .then(literal("open")
                        .then(GamePortalArgument.argument("portal").executes(GamePortalCommand::openPortal))
                    )
                )
        );
    }
    // @formatter:on

    private static int openPortal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var portal = GamePortalArgument.get(context, "portal");
        portal.requestJoin(context.getSource().getPlayer(), false);
        return 1;
    }

    private static int connectEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var portal = GamePortalArgument.get(context, "portal");

        var entity = EntityArgument.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface portalInterface) {
            if (!portal.addInterface(portalInterface)) {
                throw INTERFACE_ALREADY_CONNECTED.create();
            }

            context.getSource().sendSuccess(() -> {
                var message = Component.translatable("text.plasmid.game.portal.connect.entity", Component.translationArg(portal.getId()), entity.getName());
                return message.withStyle(ChatFormatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int connectBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var world = source.getLevel();

        var portal = GamePortalArgument.get(context, "portal");
        var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GamePortalInterface portalInterface) {
            if (!portal.addInterface(portalInterface)) {
                throw INTERFACE_ALREADY_CONNECTED.create();
            }

            source.sendSuccess(() -> {
                var message = Component.translatable("text.plasmid.game.portal.connect.block", Component.translationArg(portal.getId()), pos.getX(), pos.getY(), pos.getZ());
                return message.withStyle(ChatFormatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int disconnectEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, "entity");

        if (entity instanceof GamePortalInterface portalInterface) {
            portalInterface.invalidatePortal();

            context.getSource().sendSuccess(() -> {
                var message = Component.translatable("text.plasmid.game.portal.disconnect.entity", entity.getName());
                return message.withStyle(ChatFormatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int disconnectBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var world = source.getLevel();

        var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GamePortalInterface portalInterface) {
            portalInterface.invalidatePortal();

            source.sendSuccess(() -> {
                var message = Component.translatable("text.plasmid.game.portal.disconnect.block", pos.getX(), pos.getY(), pos.getZ());
                return message.withStyle(ChatFormatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }
}
