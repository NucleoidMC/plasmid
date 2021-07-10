package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.command.argument.ChunkGeneratorArgument;
import xyz.nucleoid.plasmid.command.argument.DimensionOptionsArgument;
import xyz.nucleoid.plasmid.command.argument.MapWorkspaceArgument;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplatePlacer;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspaceManager;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceTraveler;
import xyz.nucleoid.plasmid.mixin.MinecraftServerAccessor;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MapManageCommand {
    public static final SimpleCommandExceptionType MAP_NOT_HERE = new SimpleCommandExceptionType(
            new TranslatableText("text.plasmid.map.map_not_here")
    );

    public static final DynamicCommandExceptionType MAP_ALREADY_EXISTS = new DynamicCommandExceptionType(arg ->
            new TranslatableText("text.plasmid.map.open.map_already_exists", arg)
    );

    public static final SimpleCommandExceptionType MAP_MISMATCH = new SimpleCommandExceptionType(
            new TranslatableText("text.plasmid.map.delete.map_mismatch")
    );

    public static final DynamicCommandExceptionType INVALID_GENERATOR_CONFIG = new DynamicCommandExceptionType(arg ->
            new TranslatableText("text.plasmid.map.open.invalid_generator_config", arg)
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("map").requires(source -> source.hasPermissionLevel(4))
                .then(literal("open")
                    .then(argument("workspace", IdentifierArgumentType.identifier())
                    .executes(context -> MapManageCommand.openWorkspace(context, null))
                        .then(literal("like")
                            .then(DimensionOptionsArgument.argument("dimension")
                            .executes(MapManageCommand::openWorkspaceLikeDimension)
                        ))
                        .then(literal("with")
                            .then(ChunkGeneratorArgument.argument("generator")
                            .then(argument("config", NbtCompoundArgumentType.nbtCompound())
                            .executes(MapManageCommand::openWorkspaceByGenerator)
                        )))
                ))
                .then(literal("origin")
                    .then(MapWorkspaceArgument.argument("workspace")
                    .then(argument("origin", BlockPosArgumentType.blockPos())
                    .executes(MapManageCommand::setWorkspaceOrigin)
                )))
                .then(literal("bounds")
                    .then(MapWorkspaceArgument.argument("workspace")
                        .executes(MapManageCommand::getWorkspaceBounds)
                        .then(argument("min", BlockPosArgumentType.blockPos())
                            .then(argument("max", BlockPosArgumentType.blockPos())
                            .executes(MapManageCommand::setWorkspaceBounds)
                        ))
                ))
                .then(literal("join")
                    .then(MapWorkspaceArgument.argument("workspace")
                    .executes(MapManageCommand::joinWorkspace)
                ))
                .then(literal("leave").executes(MapManageCommand::leaveMap))
                .then(literal("export")
                    .then(MapWorkspaceArgument.argument("workspace")
                    .executes(context -> MapManageCommand.exportMap(context, false))
                    .then(literal("withEntities")
                        .executes(context -> MapManageCommand.exportMap(context, true))
                    )
                ))
                .then(literal("delete")
                    .then(MapWorkspaceArgument.argument("workspace_once")
                    .then(MapWorkspaceArgument.argument("workspace_again")
                    .executes(MapManageCommand::deleteWorkspace)
                )))
                .then(literal("import")
                    .then(argument("location", IdentifierArgumentType.identifier())
                    .then(argument("to_workspace", IdentifierArgumentType.identifier())
                        .then(argument("origin", BlockPosArgumentType.blockPos())
                            .executes(context -> {
                                BlockPos origin = BlockPosArgumentType.getBlockPos(context, "origin");
                                return MapManageCommand.importWorkspace(context, origin);
                            })
                        )
                    .executes(context -> MapManageCommand.importWorkspace(context, BlockPos.ORIGIN))
                )))
        );
    }
    // @formatter:on

    private static int openWorkspace(CommandContext<ServerCommandSource> context, RuntimeWorldConfig worldConfig) throws CommandSyntaxException {
        var source = context.getSource();

        var givenIdentifier = IdentifierArgumentType.getIdentifier(context, "workspace");

        Identifier identifier;
        if (givenIdentifier.getNamespace().equals("minecraft")) {
            var sourceName = context.getSource().getName()
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("\\s", "_");
            identifier = new Identifier(sourceName, givenIdentifier.getPath());
        } else {
            identifier = givenIdentifier;
        }

        var workspaceManager = MapWorkspaceManager.get(source.getServer());
        if (workspaceManager.byId(identifier) != null) {
            throw MAP_ALREADY_EXISTS.create(identifier);
        }

        source.getServer().submit(() -> {
            try {
                if (worldConfig != null) {
                    workspaceManager.open(identifier, worldConfig);
                } else {
                    workspaceManager.open(identifier);
                }

                source.sendFeedback(
                        new TranslatableText("text.plasmid.map.open.success",
                                identifier,
                                new TranslatableText("text.plasmid.map.open.join_command", identifier).formatted(Formatting.GRAY)),
                        false
                );
            } catch (Throwable throwable) {
                source.sendError(new TranslatableText("text.plasmid.map.open.error"));
                Plasmid.LOGGER.error("Failed to open workspace", throwable);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int openWorkspaceLikeDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var dimension = DimensionOptionsArgument.get(context, "dimension");
        var worldConfig = new RuntimeWorldConfig()
                .setDimensionType(dimension.getDimensionType())
                .setGenerator(dimension.getChunkGenerator());

        return MapManageCommand.openWorkspace(context, worldConfig);
    }

    private static int openWorkspaceByGenerator(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var generatorCodec = ChunkGeneratorArgument.get(context, "generator");
        var config = NbtCompoundArgumentType.getNbtCompound(context, "config");

        var server = context.getSource().getServer();
        var ops = RegistryOps.of(
                NbtOps.INSTANCE,
                ((MinecraftServerAccessor) server).getServerResourceManager().getResourceManager(),
                server.getRegistryManager()
        );

        var result = generatorCodec.parse(ops, config);

        var error = result.error();
        if (error.isPresent()) {
            throw INVALID_GENERATOR_CONFIG.create(error.get());
        }

        var chunkGenerator = result.result().get();

        var worldConfig = new RuntimeWorldConfig()
                .setDimensionType(DimensionType.OVERWORLD_REGISTRY_KEY)
                .setGenerator(chunkGenerator);
        return MapManageCommand.openWorkspace(context, worldConfig);
    }

    private static int setWorkspaceOrigin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();

        var workspace = MapWorkspaceArgument.get(context, "workspace");
        var origin = BlockPosArgumentType.getBlockPos(context, "origin");

        workspace.setOrigin(origin);

        source.sendFeedback(new TranslatableText("text.plasmid.map.origin.set"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int getWorkspaceBounds(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();

        var workspace = MapWorkspaceArgument.get(context, "workspace");
        var bounds = workspace.getBounds();

        source.sendFeedback(new TranslatableText("text.plasmid.map.bounds.get", getClickablePosText(bounds.getMin()), getClickablePosText(bounds.getMax())), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int setWorkspaceBounds(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();

        var workspace = MapWorkspaceArgument.get(context, "workspace");
        var min = BlockPosArgumentType.getBlockPos(context, "min");
        var max = BlockPosArgumentType.getBlockPos(context, "max");

        workspace.setBounds(new BlockBounds(min, max));

        source.sendFeedback(new TranslatableText("text.plasmid.map.bounds.set"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int joinWorkspace(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayer();

        var workspace = MapWorkspaceArgument.get(context, "workspace");

        var workspaceWorld = workspace.getWorld();

        var returnPosition = WorkspaceTraveler.getReturnFor(player, workspaceWorld.getRegistryKey());
        if (returnPosition != null) {
            returnPosition.applyTo(player);
        } else {
            player.teleport(workspaceWorld, 0.0, 64.0, 0.0, 0.0F, 0.0F);
        }

        if (player.getAbilities().allowFlying) {
            player.getAbilities().flying = true;
            player.sendAbilitiesUpdate();
        }

        source.sendFeedback(
                new TranslatableText("text.plasmid.map.join.success",
                        workspace.getIdentifier(),
                        new TranslatableText("text.plasmid.map.join.leave_command").formatted(Formatting.GRAY)),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int leaveMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayer();

        var workspaceManager = MapWorkspaceManager.get(source.getServer());
        var workspace = workspaceManager.byDimension(player.world.getRegistryKey());

        if (workspace == null) {
            throw MAP_NOT_HERE.create();
        }

        var returnPosition = WorkspaceTraveler.getLeaveReturn(player);
        if (returnPosition != null) {
            returnPosition.applyTo(player);
        } else {
            var overworld = source.getServer().getOverworld();
            var spawnPos = overworld.getSpawnPos();
            player.teleport(overworld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0.0F, 0.0F);
        }

        source.sendFeedback(
                new TranslatableText("text.plasmid.map.leave.success", workspace.getIdentifier()),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int exportMap(CommandContext<ServerCommandSource> context, boolean includeEntities) throws CommandSyntaxException {
        var source = context.getSource();

        var workspace = MapWorkspaceArgument.get(context, "workspace");

        var template = workspace.compile(includeEntities);

        var bounds = template.getBounds();
        if (bounds.getMin().getY() < 0 || bounds.getMax().getY() > 255) {
            source.sendFeedback(
                    new TranslatableText("text.plasmid.map.export.vertical_bounds_warning.line.1").append("\n")
                            .append(new TranslatableText("text.plasmid.map.export.vertical_bounds_warning.line.2")).append("\n")
                            .append(new TranslatableText("text.plasmid.map.export.vertical_bounds_warning.line.3"))
                            .formatted(Formatting.YELLOW),
                    false
            );
        }

        var future = MapTemplateSerializer.INSTANCE.saveToExport(template, workspace.getIdentifier());

        future.handle((v, throwable) -> {
            if (throwable == null) {
                source.sendFeedback(new TranslatableText("text.plasmid.map.export.success", workspace.getIdentifier()), false);
            } else {
                Plasmid.LOGGER.error("Failed to export map to '{}'", workspace.getIdentifier(), throwable);
                source.sendError(new TranslatableText("text.plasmid.map.export.error"));
            }
            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int deleteWorkspace(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();

        var workspace = MapWorkspaceArgument.get(context, "workspace_once");
        var workspaceAgain = MapWorkspaceArgument.get(context, "workspace_again");
        if (workspace != workspaceAgain) {
            throw MAP_MISMATCH.create();
        }

        var workspaceManager = MapWorkspaceManager.get(source.getServer());

        MutableText message;
        if (workspaceManager.delete(workspace)) {
            message = new TranslatableText("text.plasmid.map.delete.success", workspace.getIdentifier());
        } else {
            message = new TranslatableText("text.plasmid.map.delete.error", workspace.getIdentifier());
        }

        source.sendFeedback(message.formatted(Formatting.RED), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int importWorkspace(CommandContext<ServerCommandSource> context, BlockPos origin) throws CommandSyntaxException {
        var source = context.getSource();

        var location = IdentifierArgumentType.getIdentifier(context, "location");
        var toWorkspaceId = IdentifierArgumentType.getIdentifier(context, "to_workspace");

        var workspaceManager = MapWorkspaceManager.get(source.getServer());
        if (workspaceManager.byId(toWorkspaceId) != null) {
            throw MAP_ALREADY_EXISTS.create(toWorkspaceId);
        }

        var future = tryLoadTemplateForImport(location);

        future.thenAcceptAsync(template -> {
            if (template != null) {
                source.sendFeedback(new TranslatableText("text.plasmid.map.import.importing"), false);

                var workspace = workspaceManager.open(toWorkspaceId);

                workspace.setBounds(template.getBounds().offset(origin));
                workspace.setOrigin(origin);

                for (var region : template.getMetadata().getRegions()) {
                    workspace.addRegion(region.getMarker(), region.getBounds().offset(origin), region.getData());
                }

                workspace.setData(template.getMetadata().getData());

                try {
                    var placer = new MapTemplatePlacer(template);
                    placer.placeAt(workspace.getWorld(), origin);
                    source.sendFeedback(new TranslatableText("text.plasmid.map.import.success", toWorkspaceId), false);
                } catch (Exception e) {
                    Plasmid.LOGGER.error("Failed to place template into world!", e);
                }
            } else {
                source.sendError(new TranslatableText("text.plasmid.map.import.no_template_found", location));
            }
        }, source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<MapTemplate> tryLoadTemplateForImport(Identifier location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return MapTemplateSerializer.INSTANCE.loadFromExport(location);
            } catch (IOException ignored) {
                try {
                    return MapTemplateSerializer.INSTANCE.loadFromResource(location);
                } catch (IOException e) {
                    Plasmid.LOGGER.error("Failed to import workspace at {}", location, e);
                    return null;
                }
            }
        }, Util.getIoWorkerExecutor());
    }

    protected static Text getClickablePosText(BlockPos pos) {
        var linkCommand = "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        var linkStyle = Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, linkCommand))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip")))
                .withFormatting(Formatting.GREEN);

        return Texts.bracketed(new TranslatableText("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())).setStyle(linkStyle);
    }
}
