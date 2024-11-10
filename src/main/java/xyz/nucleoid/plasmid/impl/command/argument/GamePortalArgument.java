package xyz.nucleoid.plasmid.impl.command.argument;

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
import xyz.nucleoid.plasmid.impl.portal.GamePortal;
import xyz.nucleoid.plasmid.impl.portal.GamePortalManager;

public final class GamePortalArgument {
    private static final SimpleCommandExceptionType PORTAL_NOT_FOUND = new SimpleCommandExceptionType(Text.translatable("text.plasmid.portal.portal_not_found"));

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    var portalManager = GamePortalManager.INSTANCE;

                    return CommandSource.suggestIdentifiers(
                            portalManager.getPortals().stream().map(GamePortal::getId),
                            builder
                    );
                });
    }

    public static GamePortal get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var identifier = IdentifierArgumentType.getIdentifier(context, name);

        var portal = GamePortalManager.INSTANCE.byId(identifier);
        if (portal == null) {
            throw PORTAL_NOT_FOUND.create();
        }

        return portal;
    }
}
