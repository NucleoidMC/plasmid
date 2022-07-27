package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShoutCommand {
    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("shout")
                .then(argument("message", MessageArgumentType.message())
                .executes(ShoutCommand::sendMessage))
        );
    }
    // @formatter:on

    public static int sendMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var server = source.getServer();

        final MessageArgumentType.SignedMessage argument = MessageArgumentType.getSignedMessage(context, "message");

        try {
            var hasChatChannel = (HasChatChannel) source.getPlayerOrThrow();
            argument.decorate(source, message -> {
                var old = hasChatChannel.getChatChannel();
                try {
                    hasChatChannel.setChatChannel(ChatChannel.ALL);
                    server.getPlayerManager().broadcast(message, source, MessageType.params(MessageType.CHAT, source));
                } finally {
                    hasChatChannel.setChatChannel(old);
                }
            });
        } catch (final CommandSyntaxException e) {
            argument.sendHeader(source);
            throw e;
        }

        return Command.SINGLE_SUCCESS;
    }
}
