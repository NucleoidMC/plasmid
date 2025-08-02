package xyz.nucleoid.plasmid.impl.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

import java.util.Locale;

public final class GameConfigArgument {
    private static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(id ->
            Text.stringifiedTranslatable("text.plasmid.game_config.game_not_found", id)
    );

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((ctx, builder) -> {
                    var registry = ctx.getSource().getRegistryManager().getOrThrow(PlasmidRegistryKeys.GAME_CONFIG);
                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                    CommandSource.forEachMatching(registry.getKeys(), remaining, RegistryKey::getValue, key -> {
                        registry.getOptional(key).ifPresent(entry -> {
                            builder.suggest(key.getValue().toString(), GameConfig.name(entry));
                        });
                    });
                    return builder.buildFuture();
                });
    }

    public static RegistryEntry.Reference<GameConfig<?>> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var key = RegistryKey.of(PlasmidRegistryKeys.GAME_CONFIG, IdentifierArgumentType.getIdentifier(context, name));
        var registry = context.getSource().getRegistryManager().getOrThrow(PlasmidRegistryKeys.GAME_CONFIG);
        return registry.getOptional(key).orElseThrow(() -> GAME_NOT_FOUND.create(key.getValue()));
    }
}
