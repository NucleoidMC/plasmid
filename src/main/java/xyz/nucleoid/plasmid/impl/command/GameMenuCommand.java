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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.menu.GameMenuTheme;
import xyz.nucleoid.plasmid.impl.command.argument.GameMenuEntryArgument;
import xyz.nucleoid.plasmid.impl.command.argument.GameMenuThemeArgument;
import xyz.nucleoid.plasmid.impl.menu.GameMenuEntries;
import xyz.nucleoid.plasmid.impl.menu.GameMenuRenderer;
import xyz.nucleoid.plasmid.impl.menu.anchor.GameMenuAnchor;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static xyz.nucleoid.plasmid.impl.Plasmid.id;

public final class GameMenuCommand {
    public static final SimpleCommandExceptionType TARGET_IS_NOT_INTERFACE = new SimpleCommandExceptionType(
            Component.translatable("text.plasmid.game.menu.connect.target_is_not_interface")
    );

    // @formatter:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("menu")
                    .then(literal("connect")
                        .requires(PermissionPredicates.require(id("command/game/menu/connect"), PermissionLevel.ADMINS))
                        .then(GameMenuEntryArgument.argument("entry")
                        .then(argument("entity", EntityArgument.entity())
                            .executes(context -> connectEntity(context, null))
                            .then(GameMenuThemeArgument.argument("theme")
                                .executes(context -> connectEntity(context, GameMenuThemeArgument.get(context, "theme")))))
                        .then(argument("pos", BlockPosArgument.blockPos())
                            .executes(context -> connectBlock(context, null))
                            .then(GameMenuThemeArgument.argument("theme")
                                .executes(context -> connectBlock(context, GameMenuThemeArgument.get(context, "theme")))))
                    ))
                    .then(literal("disconnect")
                        .requires(PermissionPredicates.require(id("command/game/menu/disconnect"), PermissionLevel.ADMINS))
                        .then(argument("entity", EntityArgument.entity()).executes(GameMenuCommand::disconnectEntity))
                        .then(argument("pos", BlockPosArgument.blockPos()).executes(GameMenuCommand::disconnectBlock))
                    )
                    .then(literal("open")
                        .then(GameMenuEntryArgument.argument("entry")
                            .executes(context -> openMenu(context, null))
                            .then(GameMenuThemeArgument.argument("theme")
                                .requires(PermissionPredicates.require(id("command/game/menu/open/theme"), PermissionLevel.ADMINS))
                                .executes(context -> openMenu(context, GameMenuThemeArgument.get(context, "theme"))))
                        )
                    )
                )
        );
    }
    // @formatter:on

    /**
     * Opens whatever the entry opens, in the given theme if one was named.
     * <p>
     * The theme is forced around the click rather than looked up from the entry, so this works for any
     * clickable entry: a {@code plasmid:menu}, a hybrid, or a game that builds its menu in code. Looking it
     * up would only work for entries Plasmid can recognise as opening a named menu.
     */
    private static int openMenu(CommandContext<CommandSourceStack> context, @Nullable Holder<GameMenuTheme> theme) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var entry = GameMenuEntries.of(GameMenuEntryArgument.get(context, "entry").value());

        if (theme == null) {
            entry.click(player, false);
        } else {
            GameMenuRenderer.openWithTheme(player, theme.value(), () -> entry.click(player, false));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int connectEntity(CommandContext<CommandSourceStack> context, @Nullable Holder<GameMenuTheme> theme) throws CommandSyntaxException {
        var entry = GameMenuEntryArgument.get(context, "entry");
        var entity = EntityArgument.getEntity(context, "entity");

        if (entity instanceof GameMenuAnchor anchor) {
            anchor.bind(entry, theme);

            context.getSource().sendSuccess(() -> {
                var name = entry.value().createEntry().name();

                var message = theme == null
                        ? Component.translatable("text.plasmid.game.menu.connect.entity", name, entity.getName())
                        : Component.translatable("text.plasmid.game.menu.connect.entity.themed", name, entity.getName(), themeName(theme));

                return message.withStyle(ChatFormatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    private static int connectBlock(CommandContext<CommandSourceStack> context, @Nullable Holder<GameMenuTheme> theme) throws CommandSyntaxException {
        var source = context.getSource();
        var world = source.getLevel();

        var entry = GameMenuEntryArgument.get(context, "entry");
        var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GameMenuAnchor anchor) {
            anchor.bind(entry, theme);

            source.sendSuccess(() -> {
                var name = entry.value().createEntry().name();

                var message = theme == null
                        ? Component.translatable("text.plasmid.game.menu.connect.block", name, pos.getX(), pos.getY(), pos.getZ())
                        : Component.translatable("text.plasmid.game.menu.connect.block.themed", name, pos.getX(), pos.getY(), pos.getZ(), themeName(theme));

                return message.withStyle(ChatFormatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }

    /**
     * Themes have no name of their own, so the id it was loaded under is what there is to show.
     */
    private static String themeName(Holder<GameMenuTheme> theme) {
        return theme.unwrapKey().map(key -> key.identifier().toString()).orElse("?");
    }

    private static int disconnectEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, "entity");

        if (entity instanceof GameMenuAnchor anchor) {
            anchor.bind(null);

            context.getSource().sendSuccess(() -> {
                var message = Component.translatable("text.plasmid.game.menu.disconnect.entity", entity.getName());
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
        if (blockEntity instanceof GameMenuAnchor anchor) {
            anchor.bind(null);

            source.sendSuccess(() -> {
                var message = Component.translatable("text.plasmid.game.menu.disconnect.block", pos.getX(), pos.getY(), pos.getZ());
                return message.withStyle(ChatFormatting.GRAY);
            }, false);

            return Command.SINGLE_SUCCESS;
        } else {
            throw TARGET_IS_NOT_INTERFACE.create();
        }
    }
}
