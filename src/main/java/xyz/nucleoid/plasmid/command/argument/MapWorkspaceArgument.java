package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspace;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspaceManager;

public final class MapWorkspaceArgument {
    public static final DynamicCommandExceptionType WORKSPACE_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("text.plasmid.map_workspace.workspace_not_found", arg)
    );

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    ServerCommandSource source = context.getSource();
                    MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getMinecraftServer());

                    return CommandSource.suggestIdentifiers(
                            workspaceManager.getWorkspaceIds().stream(),
                            builder
                    );
                });
    }

    public static MapWorkspace get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        ServerCommandSource source = context.getSource();
        MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(source.getMinecraftServer());

        MapWorkspace workspace = workspaceManager.byId(identifier);
        if (workspace == null) {
            throw WORKSPACE_NOT_FOUND.create(identifier);
        }

        return workspace;
    }
}
