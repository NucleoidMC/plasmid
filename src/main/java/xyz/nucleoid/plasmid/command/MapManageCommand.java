package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.command.argument.MapWorkspaceArgument;
import xyz.nucleoid.plasmid.game.map.template.*;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MapManageCommand {
    public static final SimpleCommandExceptionType MAP_NOT_HERE = new SimpleCommandExceptionType(
            new LiteralText("No map found here")
    );

    public static final DynamicCommandExceptionType MAP_ALREADY_EXISTS = new DynamicCommandExceptionType(arg ->
            new TranslatableText("Map with id '%s' already exists!", arg)
    );

    public static final SimpleCommandExceptionType MAP_MISMATCH = new SimpleCommandExceptionType(
            new LiteralText("The given workspaces do not match! Are you sure you want to delete that?")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // TODO: import functionality
        dispatcher.register(
            literal("map").requires(source -> source.hasPermissionLevel(4))
                .then(literal("open")
                    .then(argument("workspace", IdentifierArgumentType.identifier())
                    .executes(MapManageCommand::openWorkspace)
                ))
                .then(literal("origin")
                    .then(MapWorkspaceArgument.argument("workspace")
                    .then(argument("origin", BlockPosArgumentType.blockPos())
                    .executes(MapManageCommand::setWorkspaceOrigin)
                )))
                .then(literal("bounds")
                    .then(MapWorkspaceArgument.argument("workspace")
                    .then(argument("min", BlockPosArgumentType.blockPos())
                    .then(argument("max", BlockPosArgumentType.blockPos())
                    .executes(MapManageCommand::setWorkspaceBounds)
                ))))
                .then(literal("join")
                    .then(MapWorkspaceArgument.argument("workspace")
                    .executes(MapManageCommand::joinWorkspace)
                ))
                .then(literal("leave").executes(MapManageCommand::leaveMap))
                .then(literal("compile")
                    .then(MapWorkspaceArgument.argument("workspace")
                    .executes(context -> MapManageCommand.compileMap(context, false))
                    .then(literal("withEntities")
                        .executes(context -> MapManageCommand.compileMap(context, true))
                    )
                ))
                .then(literal("delete")
                    .then(MapWorkspaceArgument.argument("workspace_once")
                    .then(MapWorkspaceArgument.argument("workspace_again")
                    .executes(MapManageCommand::deleteWorkspace)
                )))
        );
    }
    // @formatter:on

    private static int openWorkspace(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "workspace");

        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getMinecraftServer());
        if (workspaceManager.byId(identifier) != null) {
            throw MAP_ALREADY_EXISTS.create(identifier);
        }

        workspaceManager.open(identifier);

        source.sendFeedback(
                new LiteralText("Opened workspace '" + identifier + "'! Use ")
                        .append(new LiteralText("/map join " + identifier).formatted(Formatting.GRAY))
                        .append(" to join this map"),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int setWorkspaceOrigin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        MapWorkspace workspace = MapWorkspaceArgument.get(context, "workspace");
        BlockPos origin = BlockPosArgumentType.getBlockPos(context, "origin");

        workspace.setOrigin(origin);

        source.sendFeedback(new LiteralText("Updated origin for workspace"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int setWorkspaceBounds(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        MapWorkspace workspace = MapWorkspaceArgument.get(context, "workspace");
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");

        workspace.setBounds(new BlockBounds(min, max));

        source.sendFeedback(new LiteralText("Updated bounds for workspace"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinWorkspace(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        MapWorkspace workspace = MapWorkspaceArgument.get(context, "workspace");

        ServerWorld workspaceWorld = workspace.getWorld();

        ReturnPosition returnPosition = WorkspaceTraveler.getReturnFor(player, workspaceWorld.getRegistryKey());
        if (returnPosition != null) {
            returnPosition.applyTo(player);
        } else {
            player.teleport(workspaceWorld, 0.0, 64.0, 0.0, 0.0F, 0.0F);
        }

        if (player.abilities.allowFlying) {
            player.abilities.flying = true;
            player.sendAbilitiesUpdate();
        }

        source.sendFeedback(
                new LiteralText("You have joined '" + workspace.getIdentifier() + "'! Use ")
                        .append(new LiteralText("/map leave").formatted(Formatting.GRAY))
                        .append(" to return to your original position"),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int leaveMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getMinecraftServer());
        MapWorkspace workspace = workspaceManager.byDimension(player.world.getRegistryKey());

        if (workspace == null) {
            throw MAP_NOT_HERE.create();
        }

        ReturnPosition returnPosition = WorkspaceTraveler.getLeaveReturn(player);
        if (returnPosition != null) {
            returnPosition.applyTo(player);
        } else {
            ServerWorld overworld = source.getMinecraftServer().getOverworld();
            BlockPos spawnPos = overworld.getSpawnPos();
            player.teleport(overworld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0.0F, 0.0F);
        }

        source.sendFeedback(
                new LiteralText("You have left '" + workspace.getIdentifier() + "'!"),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int compileMap(CommandContext<ServerCommandSource> context, boolean includeEntities) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        MapWorkspace workspace = MapWorkspaceArgument.get(context, "workspace");

        MapTemplate template = workspace.compile(includeEntities);
        CompletableFuture<Void> future = MapTemplateSerializer.INSTANCE.save(template, workspace.getIdentifier());

        future.handle((v, throwable) -> {
            if (throwable == null) {
                source.sendFeedback(new LiteralText("Compiled and saved map '" + workspace.getIdentifier() + "'"), false);
            } else {
                Plasmid.LOGGER.error("Failed to compile map to '{}'", workspace.getIdentifier(), throwable);
                source.sendError(new LiteralText("Failed to compile map! An unexpected exception was thrown"));
            }
            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int deleteWorkspace(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        MapWorkspace workspace = MapWorkspaceArgument.get(context, "workspace_once");
        MapWorkspace workspaceAgain = MapWorkspaceArgument.get(context, "workspace_again");
        if (workspace != workspaceAgain) {
            throw MAP_MISMATCH.create();
        }

        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getMinecraftServer());

        MutableText message;
        if (workspaceManager.delete(workspace)) {
            message = new LiteralText("Deleted workspace '" + workspace.getIdentifier() + "'!");
        } else {
            message = new LiteralText("Failed to delete workspace '" + workspace.getIdentifier() + "'!");
        }

        source.sendFeedback(message.formatted(Formatting.RED), false);

        return Command.SINGLE_SUCCESS;
    }
}
