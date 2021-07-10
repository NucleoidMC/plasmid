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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspace;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspaceManager;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceRegion;
import xyz.nucleoid.plasmid.map.workspace.editor.WorkspaceEditor;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MapMetadataCommand {
    public static final DynamicCommandExceptionType ENTITY_TYPE_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("text.plasmid.map.region.entity.filter.entity_type_not_found", arg)
    );

    public static final SimpleCommandExceptionType MAP_NOT_HERE = MapManageCommand.MAP_NOT_HERE;

    public static final SimpleCommandExceptionType NO_REGION_READY = new SimpleCommandExceptionType(
            new TranslatableText("text.plasmid.map.region.commit.no_region_ready")
    );

    private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            new TranslatableText("commands.data.merge.failed")
    );

    private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType(
            new TranslatableText("commands.data.get.multiple")
    );

    private static final DynamicCommandExceptionType MODIFY_EXPECTED_OBJECT_EXCEPTION = new DynamicCommandExceptionType(
            arg -> new TranslatableText("commands.data.modify.expected_object", arg)
    );

    private static final NbtTextFormatter NBT_FORMATTER = new NbtTextFormatter("  ", 0);

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("map").requires(source -> source.hasPermissionLevel(4))
                .then(literal("region")
                    .then(literal("add")
                        .then(argument("marker", StringArgumentType.word())
                        .then(argument("min", BlockPosArgumentType.blockPos())
                        .then(argument("max", BlockPosArgumentType.blockPos())
                        .executes(MapMetadataCommand::addRegion)
                        .then(argument("data", NbtCompoundArgumentType.nbtCompound())
                        .executes(context -> addRegion(context, NbtCompoundArgumentType.getNbtCompound(context, "data")))
                    )))))
                    .then(literal("rename")
                        .then(literal("all")
                            .then(argument("old", StringArgumentType.word()).suggests(regionSuggestions())
                            .then(argument("new", StringArgumentType.word())
                            .executes(context -> renameRegions(context, (region, oldMarker, pos) -> region.marker.equals(oldMarker)))
                        )))
                        .then(literal("here")
                            .then(argument("old", StringArgumentType.word()).suggests(localRegionSuggestions())
                            .then(argument("new", StringArgumentType.word())
                            .executes(
                                context -> renameRegions(context, (region, oldMarker, pos) -> region.marker.equals(oldMarker)
                                        && region.bounds.contains(pos))
                            )
                        )))
                    )
                    .then(literal("bounds")
                        .then(argument("marker", StringArgumentType.word()).suggests(regionSuggestions())
                        .executes(MapMetadataCommand::getRegionBounds))
                    )
                    .then(literal("data")
                        .then(argument("marker", StringArgumentType.word()).suggests(localRegionSuggestions())
                            .then(literal("get").executes(executeInRegions("", MapMetadataCommand::executeRegionDataGet)))
                            .then(literal("merge")
                                .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                    .executes(executeInRegions("Merged data in %d regions.", MapMetadataCommand::executeRegionDataMerge))
                            ))
                            .then(literal("set")
                                .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                    .executes(executeInRegions("Set data in %d regions.", MapMetadataCommand::executeRegionDataSet))
                            ))
                            .then(literal("remove")
                                .then(argument("path", NbtPathArgumentType.nbtPath())
                                    .executes(executeInRegions("Removed data in %d regions.", MapMetadataCommand::executeRegionDataRemove))
                            ))
                    ))
                    .then(literal("remove")
                        .then(literal("here")
                            .executes(MapMetadataCommand::removeRegionHere)
                        )
                        .then(literal("at")
                            .then(argument("pos", BlockPosArgumentType.blockPos())
                            .executes(MapMetadataCommand::removeRegionAt)
                        ))
                    )
                    .then(literal("commit")
                        .then(argument("marker", StringArgumentType.word())
                        .executes(MapMetadataCommand::commitRegion)
                        .then(argument("data", NbtCompoundArgumentType.nbtCompound())
                        .executes(context -> commitRegion(context, NbtCompoundArgumentType.getNbtCompound(context, "data")))
                    )))
                )
                .then(literal("entity")
                    .then(literal("add")
                        .then(argument("entities", EntityArgumentType.entities())
                        .executes(MapMetadataCommand::addEntities)
                    ))
                    .then(literal("remove")
                        .then(argument("entities", EntityArgumentType.entities())
                        .executes(MapMetadataCommand::removeEntities)
                    ))
                    .then(literal("filter")
                        .then(literal("type")
                            .then(literal("add")
                                .then(argument("entity_type", IdentifierArgumentType.identifier()).suggests(entityTypeSuggestions())
                                .executes(MapMetadataCommand::addEntityType)
                            ))
                            .then(literal("remove")
                                .then(argument("entity_type", IdentifierArgumentType.identifier()).suggests(entityTypeSuggestions())
                                .executes(MapMetadataCommand::removeEntityType)
                            ))
                        )
                    )
                )
                .then(literal("data")
                        .then(literal("get")
                            .executes(MapMetadataCommand::executeDataGet)
                            .then(literal("at")
                                .then(argument("path", NbtPathArgumentType.nbtPath())
                                .executes(MapMetadataCommand::executeDataGetAt)
                        )))
                        .then(literal("merge")
                            .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                .executes(MapMetadataCommand::executeDataMerge)
                            )
                            .then(argument("nbt", NbtElementArgumentType.nbtElement())
                                .then(literal("at")
                                .then(argument("path", NbtPathArgumentType.nbtPath())
                                .executes(MapMetadataCommand::executeDataMergeAt)
                            )))
                        )
                        .then(literal("remove")
                            .executes(context -> executeDataRemove(context, null))
                            .then(literal("at")
                                .then(argument("path", NbtPathArgumentType.nbtPath())
                                .executes(context -> executeDataRemove(context, NbtPathArgumentType.getNbtPath(context, "path")))
                        )))
                        .then(literal("set")
                            .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                .executes(MapMetadataCommand::executeDataSet)
                            )
                            .then(literal("at")
                                .then(argument("path", NbtPathArgumentType.nbtPath())
                                    .then(argument("nbt", NbtElementArgumentType.nbtElement())
                                    .executes(MapMetadataCommand::executeDataSetAt)
                            )))
                        )
                )
        );
    }
    // @formatter:on

    private static int addRegion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return addRegion(context, new NbtCompound());
    }

    private static int addRegion(CommandContext<ServerCommandSource> context, NbtCompound data) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        String marker = StringArgumentType.getString(context, "marker");
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");

        MapWorkspace map = getWorkspaceForSource(source);
        map.addRegion(marker, new BlockBounds(min, max), data);
        source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.region.add.success", marker)), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int renameRegions(CommandContext<ServerCommandSource> context, RegionPredicate predicate) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        BlockPos pos = source.getPlayer().getBlockPos();

        String oldMarker = StringArgumentType.getString(context, "old");
        String newMarker = StringArgumentType.getString(context, "new");

        MapWorkspace map = getWorkspaceForSource(source);

        List<WorkspaceRegion> regions = map.getRegions().stream()
                .filter(region -> predicate.test(region, oldMarker, pos))
                .collect(Collectors.toList());

        for (WorkspaceRegion region : regions) {
            map.removeRegion(region);
            map.addRegion(newMarker, region.bounds, region.data);
        }

        source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.region.rename.success", regions.size())), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int getRegionBounds(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(source);

        String marker = StringArgumentType.getString(context, "marker");

        List<WorkspaceRegion> regions = map.getRegions().stream()
                .filter(region -> region.marker.equals(marker))
                .collect(Collectors.toList());

        source.sendFeedback(new TranslatableText("text.plasmid.map.region.bounds.get.header", regions.size()).formatted(Formatting.BOLD), false);

        for (WorkspaceRegion region : regions) {
            Text minText = MapManageCommand.getClickablePosText(region.bounds.getMin());
            Text maxText = MapManageCommand.getClickablePosText(region.bounds.getMax());

            source.sendFeedback(new TranslatableText("text.plasmid.entry", new TranslatableText("text.plasmid.map.region.bounds.get", minText, maxText)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static boolean executeRegionDataGet(CommandContext<ServerCommandSource> context, MapWorkspace map, WorkspaceRegion region) {
        var message = withMapPrefix(map,
                new TranslatableText("text.plasmid.map.region.data.get", region.marker, NBT_FORMATTER.apply(region.data))
        );
        context.getSource().sendFeedback(message, false);
        return false;
    }

    private static boolean executeRegionDataMerge(CommandContext<ServerCommandSource> context, MapWorkspace map, WorkspaceRegion region) {
        NbtCompound data = NbtCompoundArgumentType.getNbtCompound(context, "nbt");
        map.replaceRegion(region, region.withData(region.data.copy().copyFrom(data)));
        return true;
    }

    private static boolean executeRegionDataSet(CommandContext<ServerCommandSource> context, MapWorkspace map, WorkspaceRegion region) {
        NbtCompound data = NbtCompoundArgumentType.getNbtCompound(context, "nbt");
        map.replaceRegion(region, region.withData(data));
        return true;
    }

    private static boolean executeRegionDataRemove(CommandContext<ServerCommandSource> context, MapWorkspace map, WorkspaceRegion region) {
        NbtPathArgumentType.NbtPath path = NbtPathArgumentType.getNbtPath(context, "path");
        return path.remove(region.data) > 0;
    }

    private static int removeRegionHere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return removeRegion(context, context.getSource().getPlayer().getBlockPos());
    }

    private static int removeRegionAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return removeRegion(context, BlockPosArgumentType.getBlockPos(context, "pos"));
    }

    private static int removeRegion(CommandContext<ServerCommandSource> context, BlockPos pos) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(source);

        List<WorkspaceRegion> regions = map.getRegions().stream()
                .filter(region -> region.bounds.contains(pos))
                .collect(Collectors.toList());

        for (WorkspaceRegion region : regions) {
            map.removeRegion(region);
        }

        source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.region.remove.success", regions.size())), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int commitRegion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return commitRegion(context, new NbtCompound());
    }

    private static int commitRegion(CommandContext<ServerCommandSource> context, NbtCompound data) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        String marker = StringArgumentType.getString(context, "marker");

        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());
        WorkspaceEditor editor = workspaceManager.getEditorFor(player);
        if (editor != null) {
            BlockBounds region = editor.takeTracedRegion();
            if (region == null) {
                throw NO_REGION_READY.create();
            }

            BlockPos min = region.getMin();
            BlockPos max = region.getMax();

            MapWorkspace workspace = getWorkspaceForSource(source);
            workspace.addRegion(marker, new BlockBounds(min, max), data);
            source.sendFeedback(new TranslatableText("text.plasmid.map.region.add.success.excited", marker), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addEntities(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        MapWorkspace map = getWorkspaceForSource(source);

        long result = EntityArgumentType.getEntities(context, "entities").stream()
                .filter(entity -> entity.getEntityWorld().equals(world) && !(entity instanceof PlayerEntity)
                        && map.getBounds().contains(entity.getBlockPos()))
                .filter(entity -> map.addEntity(entity.getUuid()))
                .count();

        if (result == 0) {
            source.sendError(new TranslatableText("text.plasmid.map.region.entity.add.error", map.getIdentifier()));
        } else {
            source.sendFeedback(new TranslatableText("text.plasmid.map.region.entity.add.success", result, map.getIdentifier()),
                    false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeEntities(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        MapWorkspace map = getWorkspaceForSource(source);

        long result = EntityArgumentType.getEntities(context, "entities").stream()
                .filter(entity -> entity.getEntityWorld().equals(world) && !(entity instanceof PlayerEntity))
                .filter(entity -> map.removeEntity(entity.getUuid()))
                .count();

        if (result == 0) {
            source.sendError(new TranslatableText("text.plasmid.map.region.entity.remove.error", map.getIdentifier()));
        } else {
            source.sendFeedback(new TranslatableText("text.plasmid.map.region.entity.remove.success", result, map.getIdentifier()),
                    false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addEntityType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        MapWorkspace map = getWorkspaceForSource(source);
        Pair<Identifier, EntityType<?>> type = getEntityType(context);

        if (!map.addEntityType(type.getRight())) {
            source.sendError(new TranslatableText("text.plasmid.map.region.entity.filter.type.add.already_present", type.getLeft(), map.getIdentifier()));
        } else {
            source.sendFeedback(new TranslatableText("text.plasmid.map.region.entity.filter.type.add.success", type.getLeft(), map.getIdentifier()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeEntityType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        MapWorkspace map = getWorkspaceForSource(source);
        Pair<Identifier, EntityType<?>> type = getEntityType(context);

        if (!map.removeEntityType(type.getRight())) {
            source.sendError(new TranslatableText("text.plasmid.map.region.entity.filter.type.remove.not_present", type.getLeft(), map.getIdentifier()));
        } else {
            source.sendFeedback(new TranslatableText("text.plasmid.map.region.entity.filter.type.remove.success", type.getLeft(), map.getIdentifier()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataMerge(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(context.getSource());
        NbtCompound data = NbtCompoundArgumentType.getNbtCompound(context, "nbt");
        NbtCompound originalData = map.getData();
        map.setData(originalData.copy().copyFrom(data));
        source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.data.merge.success")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataMergeAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(context.getSource());

        NbtCompound sourceData = NbtCompoundArgumentType.getNbtCompound(context, "nbt");
        NbtPathArgumentType.NbtPath path = NbtPathArgumentType.getNbtPath(context, "path");

        List<NbtElement> sourceTags = path.getOrInit(sourceData, NbtCompound::new);
        List<NbtElement> mergeIntoTags = path.get(map.getData());

        int mergeCount = 0;

        for (NbtElement mergeIntoTag : mergeIntoTags) {
            if (!(mergeIntoTag instanceof NbtCompound)) {
                throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(mergeIntoTag);
            }

            NbtCompound mergedCompound = (NbtCompound) mergeIntoTag;
            NbtCompound previousCompound = mergedCompound.copy();

            for (NbtElement sourceTag : sourceTags) {
                if (!(sourceTag instanceof NbtCompound)) {
                    throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(sourceTag);
                }

                mergedCompound.copyFrom((NbtCompound) sourceTag);
            }

            if (!previousCompound.equals(mergedCompound)) {
                mergeCount++;
            }
        }

        if (mergeCount == 0) {
            throw MERGE_FAILED_EXCEPTION.create();
        }

        map.setData(map.getData());
        source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.data.merge.success")), false);

        return mergeCount;
    }

    private static int executeDataGet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(context.getSource());
        source.sendFeedback(new TranslatableText("text.plasmid.map.data.get",
                        getMapPrefix(map), NBT_FORMATTER.apply(map.getData())),
                false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataGetAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(context.getSource());
        NbtPathArgumentType.NbtPath path = NbtPathArgumentType.getNbtPath(context, "path");
        source.sendFeedback(new TranslatableText("text.plasmid.map.data.get.at",
                        map.getIdentifier().toString(), path.toString(),
                        NBT_FORMATTER.apply(getTagAt(map.getData(), path))),
                false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataRemove(CommandContext<ServerCommandSource> context, @Nullable NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(context.getSource());
        if (path == null) {
            map.setData(new NbtCompound());
            source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.data.remove.success")),
                    false);
        } else {
            int count = path.remove(map.getData());
            if (count == 0) {
                throw MERGE_FAILED_EXCEPTION.create();
            } else {
                source.sendFeedback(withMapPrefix(map,
                        new TranslatableText("text.plasmid.map.data.remove.at.success", path.toString())),
                        false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataSet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(context.getSource());
        NbtCompound data = NbtCompoundArgumentType.getNbtCompound(context, "nbt");
        map.setData(data);
        source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.data.set.success")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDataSetAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MapWorkspace map = getWorkspaceForSource(context.getSource());
        NbtPathArgumentType.NbtPath path = NbtPathArgumentType.getNbtPath(context, "path");
        NbtElement tag = NbtElementArgumentType.getNbtElement(context, "nbt");
        NbtCompound data = map.getData().copy();
        if (path.put(data, tag::copy) == 0) {
            throw MERGE_FAILED_EXCEPTION.create();
        } else {
            map.setData(data);
            source.sendFeedback(withMapPrefix(map, new TranslatableText("text.plasmid.map.data.set.at.success",
                            path.toString())),
                    false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static NbtElement getTagAt(NbtCompound data, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        Collection<NbtElement> collection = path.get(data);
        Iterator<NbtElement> iterator = collection.iterator();
        NbtElement tag = iterator.next();
        if (iterator.hasNext()) {
            throw GET_MULTIPLE_EXCEPTION.create();
        } else {
            return tag;
        }
    }

    private static Pair<Identifier, EntityType<?>> getEntityType(CommandContext<ServerCommandSource> context) throws
            CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "entity_type");
        return new Pair<>(id, Registry.ENTITY_TYPE.getOrEmpty(id).orElseThrow(() -> ENTITY_TYPE_NOT_FOUND.create(id)));
    }

    private static SuggestionProvider<ServerCommandSource> entityTypeSuggestions() {
        return (ctx, builder) -> CommandSource.suggestIdentifiers(Registry.ENTITY_TYPE.getIds(), builder);
    }

    private static SuggestionProvider<ServerCommandSource> regionSuggestions() {
        return (context, builder) -> {
            MapWorkspace map = getWorkspaceForSource(context.getSource());
            return CommandSource.suggestMatching(
                    map.getRegions().stream().map(region -> region.marker),
                    builder
            );
        };
    }

    private static SuggestionProvider<ServerCommandSource> localRegionSuggestions() {
        return (context, builder) -> {
            MapWorkspace map = getWorkspaceForSource(context.getSource());
            BlockPos sourcePos = context.getSource().getPlayer().getBlockPos();
            return CommandSource.suggestMatching(
                    map.getRegions().stream().filter(region -> region.bounds.contains(sourcePos))
                            .map(region -> region.marker),
                    builder
            );
        };
    }

    private static @NotNull MapWorkspace getWorkspaceForSource(ServerCommandSource source) throws CommandSyntaxException {
        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getServer());
        MapWorkspace workspace = workspaceManager.byDimension(source.getWorld().getRegistryKey());
        if (workspace == null) {
            throw MAP_NOT_HERE.create();
        }

        return workspace;
    }

    private static Command<ServerCommandSource> executeInRegions(String message, RegionExecutor executor) {
        return context -> {
            ServerCommandSource source = context.getSource();
            BlockPos pos = source.getPlayer().getBlockPos();

            String marker = StringArgumentType.getString(context, "marker");

            MapWorkspace map = getWorkspaceForSource(context.getSource());
            List<WorkspaceRegion> regions = map.getRegions().stream()
                    .filter(region -> region.bounds.contains(pos) && region.marker.equals(marker))
                    .collect(Collectors.toList());

            int count = 0;
            for (WorkspaceRegion region : regions) {
                if (executor.execute(context, map, region)) { count++; }
            }

            if (count > 0) {
                source.sendFeedback(withMapPrefix(map, new LiteralText(String.format(message, count))), false);
            }
            return 2;
        };
    }

    private static Text getMapPrefix(MapWorkspace map) {
        return withMapPrefix(map, null);
    }

    private static Text withMapPrefix(MapWorkspace map, @Nullable Text text) {
        MutableText prefix = new LiteralText("")
                .append(new LiteralText("[").formatted(Formatting.GRAY))
                .append(new LiteralText(map.getIdentifier().toString()).formatted(Formatting.GOLD))
                .append(new LiteralText("] ").formatted(Formatting.GRAY));
        if (text != null) prefix.append(text);
        return prefix;
    }

    @FunctionalInterface
    private interface RegionExecutor {
        boolean execute(CommandContext<ServerCommandSource> context, MapWorkspace map, WorkspaceRegion region);
    }

    @FunctionalInterface
    private interface RegionPredicate {
        boolean test(WorkspaceRegion region, String marker, BlockPos pos);
    }
}
