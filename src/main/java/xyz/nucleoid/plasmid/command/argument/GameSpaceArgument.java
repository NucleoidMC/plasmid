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
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

public final class GameSpaceArgument {
    private static final SimpleCommandExceptionType GAME_NOT_FOUND = new SimpleCommandExceptionType(Text.translatable("text.plasmid.game.not_found"));

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    var gameSpaceManager = GameSpaceManager.get();

                    return CommandSource.suggestIdentifiers(
                            gameSpaceManager.getOpenGameSpaces().stream().map(space -> space.getMetadata().userId()),
                            builder
                    );
                });
    }

    public static GameSpace get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var identifier = IdentifierArgumentType.getIdentifier(context, name);

        var gameSpace = GameSpaceManager.get().byUserId(identifier);
        if (gameSpace == null) {
            throw GAME_NOT_FOUND.create();
        }

        return gameSpace;
    }
}
