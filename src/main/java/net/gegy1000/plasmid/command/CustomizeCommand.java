package net.gegy1000.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.gegy1000.plasmid.item.CustomItem;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class CustomizeCommand {
    public static final DynamicCommandExceptionType CUSTOM_ITEM_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
        return new TranslatableText("Custom item with id '%s' was not found!", arg);
    });

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("customize")
            .requires(source -> source.hasPermissionLevel(4))
            .then(argument("custom", IdentifierArgumentType.identifier()).suggests(customSuggestions())
                .executes(CustomizeCommand::customizeHeld)
            )
        );
    }
    // @formatter:on

    private static int customizeHeld(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Identifier customId = IdentifierArgumentType.getIdentifier(context, "custom");
        CustomItem customItem = CustomItem.get(customId);

        if (customItem == null) {
            throw CUSTOM_ITEM_NOT_FOUND.create(customId);
        }

        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            customItem.applyTo(stack);
        }

        return Command.SINGLE_SUCCESS;
    }


    private static SuggestionProvider<ServerCommandSource> customSuggestions() {
        return (ctx, builder) -> {
            return CommandSource.suggestMatching(
                    CustomItem.getKeys().stream().map(Identifier::toString),
                    builder
            );
        };
    }
}
