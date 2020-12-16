package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.GameChannelManager;

public final class GameChannelArgument {
    private static final SimpleCommandExceptionType CHANNEL_NOT_FOUND = new SimpleCommandExceptionType(new LiteralText("This channel was not found!"));

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    ServerCommandSource source = context.getSource();
                    GameChannelManager channelManager = GameChannelManager.get(source.getMinecraftServer());

                    return CommandSource.suggestIdentifiers(
                            channelManager.keySet().stream(),
                            builder
                    );
                });
    }

    public static GameChannel get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        ServerCommandSource source = context.getSource();
        GameChannelManager channelManager = GameChannelManager.get(source.getMinecraftServer());

        GameChannel channel = channelManager.byId(identifier);
        if (channel == null) {
            throw CHANNEL_NOT_FOUND.create();
        }

        return channel;
    }
}
