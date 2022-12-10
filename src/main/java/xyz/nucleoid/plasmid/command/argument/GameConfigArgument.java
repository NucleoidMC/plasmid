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
import xyz.nucleoid.plasmid.game.config.GameConfigList;
import xyz.nucleoid.plasmid.game.config.GameConfigLists;
import xyz.nucleoid.plasmid.game.config.ListedGameConfig;

import java.util.Locale;
import java.util.function.Function;

public final class GameConfigArgument {
    private static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(id -> {
        return Text.translatable("text.plasmid.game_config.game_not_found", id);
    });

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((ctx, builder) -> {
                    GameConfigList list = GameConfigLists.composite();
                    Iterable<Identifier> candidates = list.keys()::iterator;
                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                    CommandSource.forEachMatching(candidates, remaining, Function.identity(), id -> {
                        var config = list.byKey(id);
                        builder.suggest(id.toString(), config.name());
                    });
                    return builder.buildFuture();
                });
    }

    public static Pair<Identifier, ListedGameConfig> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var identifier = IdentifierArgumentType.getIdentifier(context, name);

        var config = GameConfigLists.composite().byKey(identifier);
        if (config == null) {
            throw GAME_NOT_FOUND.create(identifier);
        }

        return new Pair<>(identifier, config);
    }
}
