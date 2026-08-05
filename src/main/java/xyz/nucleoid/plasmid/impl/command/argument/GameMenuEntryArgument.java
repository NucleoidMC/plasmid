package xyz.nucleoid.plasmid.impl.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import xyz.nucleoid.plasmid.api.menu.GameMenuEntryConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

public final class GameMenuEntryArgument {
    private static final SimpleCommandExceptionType NOT_FOUND =
            new SimpleCommandExceptionType(Component.translatable("text.plasmid.game.menu.entry_not_found"));

    private GameMenuEntryArgument() {
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Identifier> argument(String name) {
        return Commands.argument(name, net.minecraft.commands.arguments.IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        context.getSource().registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_MENU_ENTRY)
                                .listElementIds().map(ResourceKey::identifier),
                        builder
                ));
    }

    public static Holder<GameMenuEntryConfig> get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        var id = net.minecraft.commands.arguments.IdentifierArgument.getId(context, name);

        return context.getSource().registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_MENU_ENTRY)
                .get(ResourceKey.create(PlasmidRegistryKeys.GAME_MENU_ENTRY, id))
                .orElseThrow(NOT_FOUND::create);
    }
}
