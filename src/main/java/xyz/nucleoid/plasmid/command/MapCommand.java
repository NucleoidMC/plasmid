package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.map.template.*;
import xyz.nucleoid.plasmid.game.map.template.trace.PartialRegion;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTracer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MapCommand {
    public static final DynamicCommandExceptionType MAP_NOT_FOUND = new DynamicCommandExceptionType(arg -> new TranslatableText("Map with id '%s' was not found!", arg));

    public static final SimpleCommandExceptionType MAP_NOT_FOUND_AT = new SimpleCommandExceptionType(
            new LiteralText("No map found here")
    );

    public static final SimpleCommandExceptionType NO_REGION_READY = new SimpleCommandExceptionType(
            new LiteralText("No region ready")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("map").requires(source -> source.hasPermissionLevel(4))
                        .then(literal("stage")
                                .then(argument("identifier", IdentifierArgumentType.identifier()).suggests(stagingSuggestions())
                                        .then(argument("min", BlockPosArgumentType.blockPos())
                                                .then(argument("max", BlockPosArgumentType.blockPos())
                                                        .executes(MapCommand::stageMap)
                                                ))))
                        .then(literal("enter")
                                .then(argument("identifier", IdentifierArgumentType.identifier()).suggests(stagingSuggestions())
                                        .executes(MapCommand::enterMap)
                                ))
                        .then(literal("exit").executes(MapCommand::exitMap))
                        .then(literal("compile")
                                .then(argument("identifier", IdentifierArgumentType.identifier()).suggests(stagingSuggestions())
                                        .executes(MapCommand::compileMap)
                                ))
                        .then(literal("region")
                                .then(literal("add")
                                        .then(argument("marker", StringArgumentType.word())
                                                .then(argument("min", BlockPosArgumentType.blockPos())
                                                        .then(argument("max", BlockPosArgumentType.blockPos())
                                                                .executes(MapCommand::addRegion)
                                                        ))))
                                .then(literal("remove")
                                        .then(literal("here")
                                                .executes(MapCommand::removeRegionHere)
                                        ))
                                .then(literal("commit")
                                        .then(argument("marker", StringArgumentType.word())
                                                .executes(MapCommand::commitRegion)
                                        ))
                        )
        );
    }
    // @formatter:on

    private static int stageMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");

        StagingMapManager stagingMapManager = StagingMapManager.get(world);
        stagingMapManager.add(identifier, new BlockBounds(min, max));

        source.sendFeedback(new LiteralText("Staged map '" + identifier + "'"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int enterMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.getPlayer();

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");

        StagingMapManager stagingMapManager = StagingMapManager.get(world);
        StagingMapTemplate map = stagingMapManager.get(identifier);
        if (map == null) {
            throw MAP_NOT_FOUND.create(identifier);
        }

        if (player instanceof MapTemplateViewer) {
            MapTemplateViewer viewer = (MapTemplateViewer) player;
            viewer.setViewing(map);
            source.sendFeedback(new LiteralText("Viewing: '" + identifier + "'"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int exitMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player instanceof MapTemplateViewer) {
            MapTemplateViewer viewer = (MapTemplateViewer) player;
            StagingMapTemplate viewing = viewer.getViewing();

            if (viewing != null) {
                viewer.setViewing(null);

                Identifier identifier = viewing.getIdentifier();
                source.sendFeedback(new LiteralText("Stopped viewing: '" + identifier + "'"), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int compileMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");

        StagingMapManager stagingMapManager = StagingMapManager.get(world);
        StagingMapTemplate stagingMap = stagingMapManager.get(identifier);
        if (stagingMap == null) {
            throw MAP_NOT_FOUND.create(identifier);
        }

        MapTemplate template = stagingMap.compile();
        CompletableFuture<Void> future = MapTemplateSerializer.INSTANCE.save(template, stagingMap.getIdentifier());

        future.handle((v, throwable) -> {
            if (throwable == null) {
                source.sendFeedback(new LiteralText("Compiled and saved map '" + identifier + "'"), false);
            } else {
                Plasmid.LOGGER.error("Failed to compile map to '{}'", identifier, throwable);
                source.sendError(new LiteralText("Failed to compile map! An unexpected exception was thrown"));
            }
            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int addRegion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        StagingMapManager stagingMapManager = StagingMapManager.get(world);

        String marker = StringArgumentType.getString(context, "marker");
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        Optional<StagingMapTemplate> mapOpt = stagingMapManager.getStagingMaps().stream()
                .filter(stagingMap -> stagingMap.getBounds().contains(min) && stagingMap.getBounds().contains(max))
                .findFirst();

        if (mapOpt.isPresent()) {
            StagingMapTemplate map = mapOpt.get();
            map.addRegion(marker, new BlockBounds(min, max));
            source.sendFeedback(new LiteralText("Added region '" + marker + "' to '" + map.getIdentifier() + "'"), false);
        } else {
            throw MAP_NOT_FOUND_AT.create();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeRegionHere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = source.getPlayer().getBlockPos();

        StagingMapManager stagingMapManager = StagingMapManager.get(world);

        Optional<StagingMapTemplate> mapOpt = stagingMapManager.getStagingMaps().stream()
                .filter(stagingMap -> stagingMap.getBounds().contains(pos))
                .findFirst();

        if (mapOpt.isPresent()) {
            StagingMapTemplate map = mapOpt.get();

            List<TemplateRegion> regions = map.getRegions().stream()
                    .filter(region -> region.getBounds().contains(pos))
                    .collect(Collectors.toList());

            for (TemplateRegion region : regions) {
                map.removeRegion(region);
            }

            source.sendFeedback(new LiteralText("Removed " + regions.size() + " regions from '" + map.getIdentifier() + "'"), false);
        } else {
            throw MAP_NOT_FOUND_AT.create();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int commitRegion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = source.getWorld();

        StagingMapManager stagingMapManager = StagingMapManager.get(world);

        String marker = StringArgumentType.getString(context, "marker");

        if (player instanceof RegionTracer) {
            RegionTracer regionTracer = (RegionTracer) player;

            PartialRegion region = regionTracer.takeReady();
            if (region == null) {
                throw NO_REGION_READY.create();
            }

            BlockPos min = region.getMin();
            BlockPos max = region.getMax();

            Optional<StagingMapTemplate> mapOpt = stagingMapManager.getStagingMaps().stream()
                    .filter(stagingMap -> stagingMap.getBounds().contains(min) && stagingMap.getBounds().contains(max))
                    .findFirst();

            if (mapOpt.isPresent()) {
                StagingMapTemplate map = mapOpt.get();
                map.addRegion(marker, new BlockBounds(min, max));
                source.sendFeedback(new LiteralText("Added region '" + marker + "' to '" + map.getIdentifier() + "'"), false);
            } else {
                throw MAP_NOT_FOUND_AT.create();
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static SuggestionProvider<ServerCommandSource> stagingSuggestions() {
        return (ctx, builder) -> {
            ServerWorld world = ctx.getSource().getWorld();
            return CommandSource.suggestMatching(
                    StagingMapManager.get(world).getStagingMapKeys().stream().map(Identifier::toString),
                    builder
            );
        };
    }
}
