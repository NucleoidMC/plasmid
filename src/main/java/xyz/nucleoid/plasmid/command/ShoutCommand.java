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

        var hasChatChannel = (HasChatChannel) source.getPlayerOrThrow();
        var old = hasChatChannel.getChatChannel();
        hasChatChannel.setChatChannel(ChatChannel.ALL);

        MessageArgumentType.getSignedMessage(context, "message").decorate(source)
                .thenAcceptAsync(message -> {
                    server.getPlayerManager().broadcast(message, source, MessageType.CHAT);
                    hasChatChannel.setChatChannel(old);
                }, server);

        return Command.SINGLE_SUCCESS;
    }
}
