package xyz.nucleoid.plasmid.impl.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class GameConfigArgument {
    private static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(id ->
            Component.translatableEscape("text.plasmid.game_config.game_not_found", id)
    );

    public static RequiredArgumentBuilder<CommandSourceStack, Identifier> argument(String name) {
        return Commands.argument(name, IdentifierArgument.id())
                .suggests((ctx, builder) -> {
                    var registry = ctx.getSource().registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_CONFIG);
                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                    SharedSuggestionProvider.filterResources(registry.registryKeySet(), remaining, ResourceKey::identifier, key -> {
                        registry.get(key).ifPresent(entry -> {
                            builder.suggest(key.identifier().toString(), GameConfig.name(entry));
                        });
                    });
                    return builder.buildFuture();
                });
    }

    public static Holder.Reference<GameConfig<?>> get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        var key = ResourceKey.create(PlasmidRegistryKeys.GAME_CONFIG, IdentifierArgument.getId(context, name));
        var registry = context.getSource().registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_CONFIG);
        return registry.get(key).orElseThrow(() -> GAME_NOT_FOUND.create(key.identifier()));
    }
}
