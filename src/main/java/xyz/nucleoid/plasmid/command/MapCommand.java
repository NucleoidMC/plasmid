package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.map.template.*;
import xyz.nucleoid.plasmid.game.map.template.trace.PartialRegion;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTracer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MapCommand {
    public static final DynamicCommandExceptionType ENTITY_TYPE_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("Entity type with id '%s' was not found!", arg)
    );

    public static final DynamicCommandExceptionType MAP_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("Map with id '%s' was not found!", arg)
    );

    public static final SimpleCommandExceptionType MAP_NOT_FOUND_AT = new SimpleCommandExceptionType(
            new LiteralText("No map found here")
    );

    public static final SimpleCommandExceptionType NO_REGION_READY = new SimpleCommandExceptionType(
            new LiteralText("No region ready")
    );

    private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.data.merge.failed"));

    private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType(
            new TranslatableText("commands.data.get.multiple")
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
                    .executes(context -> MapCommand.compileMap(context, false))
                    .then(literal("withEntities")
                        .executes(context -> MapCommand.compileMap(context, true))
                    )
                ))
                .then(literal("region")
                    .then(literal("add")
                        .then(argument("marker", StringArgumentType.word())
                        .then(argument("min", BlockPosArgumentType.blockPos())
                        .then(argument("max", BlockPosArgumentType.blockPos())
                        .executes(MapCommand::addRegion)
                        .then(argument("data", NbtCompoundTagArgumentType.nbtCompound())
                        .executes(context -> addRegion(context, NbtCompoundTagArgumentType.getCompoundTag(context, "data")))
                    )))))
                    .then(literal("rename")
                        .then(literal("all")
                            .then(argument("old", StringArgumentType.word())
                            .then(argument("new", StringArgumentType.word())
                            .executes(context -> renameRegions(context, (region, oldMarker, pos) -> region.getMarker().equals(oldMarker)))
                        )))
                        .then(literal("here")
                            .then(argument("old", StringArgumentType.word())
                            .then(argument("new", StringArgumentType.word())
                            .executes(
                                context -> renameRegions(context, (region, oldMarker, pos) -> region.getMarker().equals(oldMarker)
                                        && region.getBounds().contains(pos))
                            )
                        )))
                    )
                    .then(literal("edit")
                        .then(literal("data")
                            .then(argument("marker", StringArgumentType.word())
                            .then(argument("data", NbtCompoundTagArgumentType.nbtCompound())
                            .executes(MapCommand::editRegionData)
                        )))
                    )
                    .then(literal("remove")
                        .then(literal("here")
                            .executes(MapCommand::removeRegionHere)
                        )
                        .then(literal("at")
                            .then(argument("pos", BlockPosArgumentType.blockPos())
                            .executes(MapCommand::removeRegionAt)
                        ))
                    )
                    .then(literal("commit")
                        .then(argument("marker", StringArgumentType.word())
                        .executes(MapCommand::commitRegion)
                        .then(argument("data", NbtCompoundTagArgumentType.nbtCompound())
                        .executes(context -> commitRegion(context, NbtCompoundTagArgumentType.getCompoundTag(context, "data")))
                    )))
                )
                .then(literal("entity")
                    .then(literal("add")
                        .then(argument("entities", EntityArgumentType.entities())
                        .executes(MapCommand::addEntities)
                    ))
                    .then(literal("remove")
                        .then(argument("entities", EntityArgumentType.entities())
                        .executes(MapCommand::removeEntities)
                    ))
                    .then(literal("filter")
                        .then(literal("type")
                            .then(literal("add")
                                .then(argument("entity_type", IdentifierArgumentType.identifier()).suggests(entityTypeSuggestions())
                                .executes(MapCommand::addEntityType)
                            ))
                            .then(literal("remove")
                                .then(argument("entity_type", IdentifierArgumentType.identifier()).suggests(entityTypeSuggestions())
                                .executes(MapCommand::removeEntityType)
                            ))
                        )
                    )
                )
                .then(literal("data")
                .then(argument("identifier", IdentifierArgumentType.identifier()).suggests(stagingSuggestions())
                        .then(literal("get")
                            .executes(MapCommand::executeDataGet)
                            .then(literal("at")
                                .then(argument("path", NbtPathArgumentType.nbtPath())
                                .executes(MapCommand::executeDataGetAt)
                        )))
                        .then(literal("merge")
                            .then(argument("nbt", NbtCompoundTagArgumentType.nbtCompound())
                                .executes(MapCommand::executeDataMerge)
                        ))
                        .then(literal("remove")
                            .executes(context -> executeDataRemove(context, null))
                            .then(literal("at")
                                .then(argument("path", NbtPathArgumentType.nbtPath())
                                .executes(context -> executeDataRemove(context, NbtPathArgumentType.getNbtPath(context, "path")))
                        )))
                ))
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

    private static int compileMap(CommandContext<ServerCommandSource> context, boolean includeEntities) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");

        StagingMapManager stagingMapManager = StagingMapManager.get(world);
        StagingMapTemplate stagingMap = stagingMapManager.get(identifier);
        if (stagingMap == null) {
            throw MAP_NOT_FOUND.create(identifier);
        }

        MapTemplate template = stagingMap.compile(includeEntities);
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
        return addRegion(context, new CompoundTag());
    }

    private static int addRegion(CommandContext<ServerCommandSource> context, CompoundTag data) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        String marker = StringArgumentType.getString(context, "marker");
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");

        StagingMapTemplate map = getMap(context);
        map.addRegion(marker, new BlockBounds(min, max), data);
        source.sendFeedback(new LiteralText("Added region '" + marker + "' to '" + map.getIdentifier() + "'"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int renameRegions(CommandContext<ServerCommandSource> context, RegionPredicate predicate) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        BlockPos pos = source.getPlayer().getBlockPos();

        String oldMarker = StringArgumentType.getString(context, "old");
        String newMarker = StringArgumentType.getString(context, "new");

        StagingMapTemplate map = getMap(context);

        List<TemplateRegion> regions = map.getRegions().stream()
                .filter(region -> predicate.test(region, oldMarker, pos))
                .collect(Collectors.toList());

        for (TemplateRegion region : regions) {
            map.removeRegion(region);
            map.addRegion(newMarker, region.getBounds(), region.getData());
        }

        source.sendFeedback(new LiteralText("Renamed " + regions.size() + " regions from '" + map.getIdentifier() + "'"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int editRegionData(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        BlockPos pos = source.getPlayer().getBlockPos();

        String marker = StringArgumentType.getString(context, "marker");
        CompoundTag data = NbtCompoundTagArgumentType.getCompoundTag(context, "data");

        StagingMapTemplate map = getMap(context);
        List<TemplateRegion> regions = map.getRegions().stream()
                .filter(region -> region.getBounds().contains(pos) && region.getMarker().equals(marker))
                .collect(Collectors.toList());

        for (TemplateRegion region : regions) {
            map.removeRegion(region);
            map.addRegion(marker, region.getBounds(), data);
        }

        source.sendFeedback(new LiteralText("Edited " + regions.size() + " regions from '" + map.getIdentifier() + "'"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeRegionHere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return removeRegion(context, context.getSource().getPlayer().getBlockPos());
    }

    private static int removeRegionAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return removeRegion(context, BlockPosArgumentType.getBlockPos(context, "pos"));
    }

    private static int removeRegion(CommandContext<ServerCommandSource> context, BlockPos pos) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        StagingMapTemplate map = getMap(context);

        List<TemplateRegion> regions = map.getRegions().stream()
                .filter(region -> region.getBounds().contains(pos))
                .collect(Collectors.toList());

        for (TemplateRegion region : regions) {
            map.removeRegion(region);
        }

        source.sendFeedback(new LiteralText("Removed " + regions.size() + " regions from '" + map.getIdentifier() + "'"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int commitRegion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return commitRegion(context, new CompoundTag());
    }

    private static int commitRegion(CommandContext<ServerCommandSource> context, CompoundTag data) throws CommandSyntaxException {
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
                map.addRegion(marker, new BlockBounds(min, max), data);
                source.sendFeedback(new LiteralText("Added region '" + marker + "' to '" + map.getIdentifier() + "'"), false);
            } else {
                throw MAP_NOT_FOUND_AT.create();
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addEntities(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        StagingMapTemplate map = getMap(context);

        long result = EntityArgumentType.getEntities(context, "entities").stream()
                .filter(entity -> entity.getEntityWorld().equals(world) && !(entity instanceof PlayerEntity)
                        && map.getBounds().contains(entity.getBlockPos()))
                .filter(entity -> map.addEntity(entity.getUuid()))
                .count();

        if (result == 0) {
            source.sendError(new LiteralText("Could not add entities in map \"" + map.getIdentifier() + "\"."));
        } else {
            source.sendFeedback(new LiteralText("Added " + result + " entities in map \"" + map.getIdentifier() + "\"."),
                    false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeEntities(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        StagingMapTemplate map = getMap(context);

        long result = EntityArgumentType.getEntities(context, "entities").stream()
                .filter(entity -> entity.getEntityWorld().equals(world) && !(entity instanceof PlayerEntity))
                .filter(entity -> map.removeEntity(entity.getUuid()))
                .count();

        if (result == 0) {
            source.sendError(new LiteralText("Could not remove entities in map \"" + map.getIdentifier() + "\"."));
        } else {
            source.sendFeedback(new LiteralText("Removed " + result + " entities in map \"" + map.getIdentifier() + "\"."),
                    false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addEntityType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        StagingMapTemplate map = getMap(context);
        Pair<Identifier, EntityType<?>> type = getEntityType(context);

        if (!map.addEntityType(type.getRight())) {
            source.sendError(new LiteralText("Entity type \"" + type.getLeft() + "\" is already present in map \"" + map.getIdentifier() + "\"."));
        } else {
            source.sendFeedback(new LiteralText("Added entity type \"" + type.getLeft() + "\" in map \"" + map.getIdentifier() + "\"."), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeEntityType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        StagingMapTemplate map = getMap(context);
        Pair<Identifier, EntityType<?>> type = getEntityType(context);

        if (!map.removeEntityType(type.getRight())) {
            source.sendError(new LiteralText("Entity type \"" + type.getLeft() + "\" is not present in map \"" + map.getIdentifier() + "\"."));
        } else {
            source.sendFeedback(new LiteralText("Removed entity type \"" + type.getLeft() + "\" in map \"" + map.getIdentifier() + "\"."), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataMerge(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        StagingMapTemplate map = getMap(context);
        CompoundTag data = NbtCompoundTagArgumentType.getCompoundTag(context, "nbt");
        CompoundTag originalData = map.getData();
        map.setData(originalData.copy().copyFrom(data));
        source.sendFeedback(new LiteralText("Merged map data."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataGet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        StagingMapTemplate map = getMap(context);
        source.sendFeedback(new TranslatableText("Map data of \"%s\":\n%s",
                        map.getIdentifier().toString(), map.getData().toText("  ", 0)),
                false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataGetAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        StagingMapTemplate map = getMap(context);
        NbtPathArgumentType.NbtPath path = NbtPathArgumentType.getNbtPath(context, "path");
        source.sendFeedback(new TranslatableText("Map data of \"%s\" at \"%s\":\n%s",
                        map.getIdentifier().toString(), path.toString(),
                        getTagAt(map.getData(), path).toText("  ", 0)),
                false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataRemove(CommandContext<ServerCommandSource> context, @Nullable NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        StagingMapTemplate map = getMap(context);
        if (path == null) {
            map.setData(new CompoundTag());
            source.sendFeedback(new TranslatableText("The root tag of the map data of \"%s\" is now empty.",
                    map.getIdentifier().toString()), false);
        } else {
            int count = path.remove(map.getData());
            if (count == 0) {
                throw MERGE_FAILED_EXCEPTION.create();
            } else {
                source.sendFeedback(new TranslatableText("Removed tag of map data \"%s\" at \"%s\".",
                        map.getIdentifier().toString(), path.toString()),
                        false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Tag getTagAt(CompoundTag data, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        Collection<Tag> collection = path.get(data);
        Iterator<Tag> iterator = collection.iterator();
        Tag tag = iterator.next();
        if (iterator.hasNext()) {
            throw GET_MULTIPLE_EXCEPTION.create();
        } else {
            return tag;
        }
    }

    private static Pair<Identifier, EntityType<?>> getEntityType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "entity_type");
        return new Pair<>(id, Registry.ENTITY_TYPE.getOrEmpty(id).orElseThrow(() -> ENTITY_TYPE_NOT_FOUND.create(id)));
    }

    private static SuggestionProvider<ServerCommandSource> entityTypeSuggestions() {
        return (ctx, builder) -> CommandSource.suggestIdentifiers(Registry.ENTITY_TYPE.getIds(), builder);
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

    private static @NotNull StagingMapTemplate getMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = source.getPlayer().getBlockPos();

        StagingMapManager stagingMapManager = StagingMapManager.get(world);

        Optional<StagingMapTemplate> mapOpt = stagingMapManager.getStagingMaps().stream()
                .filter(stagingMap -> stagingMap.getBounds().contains(pos))
                .findFirst();

        if (mapOpt.isPresent()) {
            return mapOpt.get();
        } else {
            throw MAP_NOT_FOUND_AT.create();
        }
    }

    @FunctionalInterface
    private interface RegionPredicate {
        boolean test(TemplateRegion region, String marker, BlockPos pos);
    }
}
