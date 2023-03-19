package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.GameConfig;
import xyz.nucleoid.plasmid.game.config.GameConfigs;

import java.util.Locale;
import java.util.function.Function;

public final class GameConfigArgument {
    private static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(id -> {
        return Text.translatable("text.plasmid.game_config.game_not_found", id);
    });

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((ctx, builder) -> {
                    Iterable<Identifier> candidates = GameConfigs.getKeys().stream()::iterator;
                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                    CommandSource.forEachMatching(candidates, remaining, Function.identity(), id -> {
                        builder.suggest(id.toString(), GameConfig.name(GameConfigs.get(id)));
                    });
                    return builder.buildFuture();
                });
    }

    public static Pair<Identifier, GameConfig<?>> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var identifier = IdentifierArgumentType.getIdentifier(context, name);

        var config = GameConfigs.get(identifier);
        if (config == null) {
            throw GAME_NOT_FOUND.create(identifier);
        }

        return new Pair<>(identifier, config);
    }
}
