package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtCompoundTagArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.command.argument.ChunkGeneratorArgument;
import xyz.nucleoid.plasmid.command.argument.DimensionOptionsArgument;
import xyz.nucleoid.plasmid.command.argument.MapWorkspaceArgument;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplatePlacer;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspace;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspaceManager;
import xyz.nucleoid.plasmid.map.workspace.ReturnPosition;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceTraveler;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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

    public static final DynamicCommandExceptionType INVALID_GENERATOR_CONFIG = new DynamicCommandExceptionType(arg ->
            new TranslatableText("Invalid generator config! %s", arg)
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
                            .then(argument("config", NbtCompoundTagArgumentType.nbtCompound())
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
                    .then(argument("min", BlockPosArgumentType.blockPos())
                    .then(argument("max", BlockPosArgumentType.blockPos())
                    .executes(MapManageCommand::setWorkspaceBounds)
                ))))
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

    private static int openWorkspace(CommandContext<ServerCommandSource> context, @Nullable Supplier<DimensionOptions> options) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        Identifier givenIdentifier = IdentifierArgumentType.getIdentifier(context, "workspace");

        Identifier identifier;
        if (givenIdentifier.getNamespace().equals("minecraft")) {
            String sourceName = context.getSource().getName()
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("\\s", "_");
            identifier = new Identifier(sourceName, givenIdentifier.getPath());
        } else {
            identifier = givenIdentifier;
        }

        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getMinecraftServer());
        if (workspaceManager.byId(identifier) != null) {
            throw MAP_ALREADY_EXISTS.create(identifier);
        }

        CompletableFuture<MapWorkspace> future;
        if (options != null) {
            future = workspaceManager.open(identifier, options);
        } else {
            future = workspaceManager.open(identifier);
        }

        future.handleAsync((workspace, throwable) -> {
            if (throwable == null) {
                source.sendFeedback(
                        new LiteralText("Opened workspace '" + identifier + "'! Use ")
                                .append(new LiteralText("/map join " + identifier).formatted(Formatting.GRAY))
                                .append(" to join this map"),
                        false
                );
            } else {
                source.sendError(new LiteralText("An unexpected error occurred while trying to open workspace!"));
                Plasmid.LOGGER.error("Failed to open workspace", throwable);
            }
            return null;
        }, source.getMinecraftServer());

        return Command.SINGLE_SUCCESS;
    }

    private static int openWorkspaceLikeDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        DimensionOptions dimension = DimensionOptionsArgument.get(context, "dimension");
        return MapManageCommand.openWorkspace(context, () -> new DimensionOptions(dimension.getDimensionTypeSupplier(), dimension.getChunkGenerator()));
    }

    private static int openWorkspaceByGenerator(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Codec<? extends ChunkGenerator> generatorCodec = ChunkGeneratorArgument.get(context, "generator");
        CompoundTag config = NbtCompoundTagArgumentType.getCompoundTag(context, "config");

        DataResult<? extends ChunkGenerator> result = generatorCodec.parse(NbtOps.INSTANCE, config);

        Optional<?> error = result.error();
        if (error.isPresent()) {
            throw INVALID_GENERATOR_CONFIG.create(error.get());
        }

        ChunkGenerator chunkGenerator = result.result().get();
        return MapManageCommand.openWorkspace(context, () -> {
            MinecraftServer server = context.getSource().getMinecraftServer();
            DimensionType dimension = server.getOverworld().getDimension();
            return new DimensionOptions(() -> dimension, chunkGenerator);
        });
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

    private static int exportMap(CommandContext<ServerCommandSource> context, boolean includeEntities) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        MapWorkspace workspace = MapWorkspaceArgument.get(context, "workspace");

        MapTemplate template = workspace.compile(includeEntities);

        BlockBounds bounds = template.getBounds();
        if (bounds.getMin().getY() < 0 || bounds.getMax().getY() > 255) {
            source.sendFeedback(
                    new LiteralText("Warning: Map exceeds vertical world boundaries!\n")
                            .append("You may want to change the map origin.\n")
                            .append("(Note: map origin corresponds to the position that will become (0,0,0) on export)")
                            .formatted(Formatting.YELLOW),
                    false
            );
        }

        CompletableFuture<Void> future = MapTemplateSerializer.INSTANCE.saveToExport(template, workspace.getIdentifier());

        future.handle((v, throwable) -> {
            if (throwable == null) {
                source.sendFeedback(new LiteralText("Compiled and exported map '" + workspace.getIdentifier() + "'"), false);
            } else {
                Plasmid.LOGGER.error("Failed to export map to '{}'", workspace.getIdentifier(), throwable);
                source.sendError(new LiteralText("Failed to export map! An unexpected exception was thrown"));
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

    private static int importWorkspace(CommandContext<ServerCommandSource> context, BlockPos origin) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        Identifier location = IdentifierArgumentType.getIdentifier(context, "location");
        Identifier toWorkspaceId = IdentifierArgumentType.getIdentifier(context, "to_workspace");

        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getMinecraftServer());
        if (workspaceManager.byId(toWorkspaceId) != null) {
            throw MAP_ALREADY_EXISTS.create(toWorkspaceId);
        }

        CompletableFuture<MapTemplate> future = tryLoadTemplateForImport(location);

        future.thenAcceptAsync(template -> {
            if (template != null) {
                workspaceManager.open(toWorkspaceId).thenAcceptAsync(workspace -> {
                    source.sendFeedback(new LiteralText("Importing workspace..."), false);

                    workspace.setBounds(template.getBounds().offset(origin));
                    workspace.setOrigin(origin);

                    for (TemplateRegion region : template.getMetadata().getRegions()) {
                        workspace.addRegion(region.getMarker(), region.getBounds().offset(origin), region.getData());
                    }

                    workspace.setData(template.getMetadata().getData());

                    try {
                        MapTemplatePlacer placer = new MapTemplatePlacer(template);
                        placer.placeAt(workspace.getWorld(), origin);
                        source.sendFeedback(new LiteralText("Imported workspace into '" + toWorkspaceId + "'!"), false);
                    } catch (Exception e) {
                        Plasmid.LOGGER.error("Failed to place template into world!", e);
                    }
                }, source.getMinecraftServer());
            } else {
                source.sendError(new LiteralText("No template found at '" + location + "'!"));
            }
        }, source.getMinecraftServer());

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
}
