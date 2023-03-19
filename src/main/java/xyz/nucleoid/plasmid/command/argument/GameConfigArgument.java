package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public final class GameConfigArgument {
    private static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(id -> {
        return Text.translatable("text.plasmid.game_config.game_not_found", id);
    });

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((ctx, builder) -> {
                    var registry = ctx.getSource().getRegistryManager().get(GameConfigs.REGISTRY_KEY);
                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                    CommandSource.forEachMatching(registry.getKeys(), remaining, RegistryKey::getValue, key -> {
                        registry.getEntry(key).ifPresent(entry -> {
                            builder.suggest(key.getValue().toString(), GameConfig.name(entry));
                        });
                    });
                    return builder.buildFuture();
                });
    }

    public static RegistryEntry.Reference<GameConfig<?>> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var key = RegistryKey.of(GameConfigs.REGISTRY_KEY, IdentifierArgumentType.getIdentifier(context, name));
        var registry = context.getSource().getRegistryManager().get(GameConfigs.REGISTRY_KEY);
        return registry.getEntry(key).orElseThrow(() -> GAME_NOT_FOUND.create(key.getValue()));
    }
}
