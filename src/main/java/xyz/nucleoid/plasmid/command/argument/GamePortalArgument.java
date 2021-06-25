package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.portal.GamePortal;
import xyz.nucleoid.plasmid.game.portal.GamePortalManager;

public final class GamePortalArgument {
    private static final SimpleCommandExceptionType PORTAL_NOT_FOUND = new SimpleCommandExceptionType(new TranslatableText("text.plasmid.portal.portal_not_found"));

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    GamePortalManager portalManager = GamePortalManager.INSTANCE;

                    return CommandSource.suggestIdentifiers(
                            portalManager.getPortals().stream().map(GamePortal::getId),
                            builder
                    );
                });
    }

    public static GamePortal get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        GamePortal portal = GamePortalManager.INSTANCE.byId(identifier);
        if (portal == null) {
            throw PORTAL_NOT_FOUND.create();
        }

        return portal;
    }
}
