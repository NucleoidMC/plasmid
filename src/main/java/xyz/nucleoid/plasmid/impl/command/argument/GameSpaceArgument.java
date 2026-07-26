package xyz.nucleoid.plasmid.impl.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;

public final class GameSpaceArgument {
    private static final SimpleCommandExceptionType GAME_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("text.plasmid.game.not_found"));

    public static RequiredArgumentBuilder<CommandSourceStack, Identifier> argument(String name) {
        return Commands.argument(name, IdentifierArgument.id())
                .suggests((context, builder) -> {
                    var gameSpaceManager = GameSpaceManagerImpl.get();

                    return SharedSuggestionProvider.suggestResource(
                            gameSpaceManager.getOpenGameSpaces().stream()
                                    .filter((space -> space.isPlayerAllowed(PlayerRef.of(context.getSource().getPlayer()))))
                                    .map(space -> space.getMetadata().userId()),
                            builder
                    );
                });
    }

    public static GameSpace get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        var identifier = IdentifierArgument.getId(context, name);

        var gameSpace = GameSpaceManagerImpl.get().byUserId(identifier);
        if (gameSpace == null) {
            throw GAME_NOT_FOUND.create();
        }

        return gameSpace;
    }
}
