package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.ConfiguredGame;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

import java.util.Locale;
import java.util.function.Function;

public final class GameConfigArgument {
    private static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(id -> {
        return new TranslatableText("text.plasmid.game_config.game_not_found", id);
    });

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((ctx, builder) -> {
                    Iterable<Identifier> candidates = GameConfigs.getKeys().stream()::iterator;
                    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                    CommandSource.forEachMatching(candidates, remaining, Function.identity(), id -> {
                        builder.suggest(id.toString(), GameConfigs.get(id).getNameText());
                    });
                    return builder.buildFuture();
                });
    }

    public static Pair<Identifier, ConfiguredGame<?>> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        ConfiguredGame<?> configuredGame = GameConfigs.get(identifier);
        if (configuredGame == null) {
            throw GAME_NOT_FOUND.create(identifier);
        }

        return new Pair<>(identifier, configuredGame);
    }
}
