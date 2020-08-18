package xyz.nucleoid.plasmid.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import xyz.nucleoid.plasmid.game.channel.GameChannel;
import xyz.nucleoid.plasmid.game.channel.GameChannelManager;

public final class GameChannelArgument {
    private static final DynamicCommandExceptionType CHANNEL_NOT_FOUND = new DynamicCommandExceptionType(id -> {
        return new TranslatableText("Channel config with id '%s' was not found!", id);
    });

    public static RequiredArgumentBuilder<ServerCommandSource, Identifier> argument(String name) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    ServerCommandSource source = context.getSource();
                    GameChannelManager channelManager = GameChannelManager.get(source.getMinecraftServer());

                    return CommandSource.suggestIdentifiers(
                            channelManager.getKeys().stream(),
                            builder
                    );
                });
    }

    public static Pair<Identifier, GameChannel> get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, name);

        ServerCommandSource source = context.getSource();
        GameChannelManager channelManager = GameChannelManager.get(source.getMinecraftServer());

        GameChannel channel = channelManager.get(identifier);
        if (channel == null) {
            throw CHANNEL_NOT_FOUND.create(identifier);
        }

        return new Pair<>(identifier, channel);
    }
}
