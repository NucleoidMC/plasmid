package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpaceLists;
import xyz.nucleoid.plasmid.game.ListedGameSpace;

public final class GameSpaceArgument {
    private static final SimpleCommandExceptionType GAME_NOT_FOUND = new SimpleCommandExceptionType(Text.translatable("text.plasmid.game.not_found"));

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    var gameSpaceList = GameSpaceLists.composite();

                    return CommandSource.suggestIdentifiers(
                            gameSpaceList.getOpenGameSpaces().stream().map(space -> space.getMetadata().userId()),
                            builder
                    );
                });
    }

    public static ListedGameSpace get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var identifier = IdentifierArgumentType.getIdentifier(context, name);

        var gameSpace = GameSpaceLists.composite().byUserId(identifier);
        if (gameSpace == null) {
            throw GAME_NOT_FOUND.create();
        }

        return gameSpace;
    }
}
