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
import xyz.nucleoid.plasmid.impl.portal.GamePortal;
import xyz.nucleoid.plasmid.impl.portal.GamePortalManager;

public final class GamePortalArgument {
    private static final SimpleCommandExceptionType PORTAL_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("text.plasmid.portal.portal_not_found"));

    public static RequiredArgumentBuilder<CommandSourceStack, Identifier> argument(String name) {
        return Commands.argument(name, IdentifierArgument.id())
                .suggests((context, builder) -> {
                    var portalManager = GamePortalManager.INSTANCE;

                    return SharedSuggestionProvider.suggestResource(
                            portalManager.getPortals().stream().map(GamePortal::getId),
                            builder
                    );
                });
    }

    public static GamePortal get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        var identifier = IdentifierArgument.getId(context, name);

        var portal = GamePortalManager.INSTANCE.byId(identifier);
        if (portal == null) {
            throw PORTAL_NOT_FOUND.create();
        }

        return portal;
    }
}
