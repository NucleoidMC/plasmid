package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import xyz.nucleoid.plasmid.api.chat.ChatChannel;
import xyz.nucleoid.plasmid.api.chat.HasChatChannel;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ShoutCommand {
    // @formatter:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("shout")
                .then(argument("message", MessageArgument.message())
                .executes(ShoutCommand::sendMessage))
        );
    }
    // @formatter:on

    public static int sendMessage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var server = source.getServer();
        var hasChatChannel = (HasChatChannel) source.getPlayerOrException();
        MessageArgument.resolveChatMessage(context, "message", message -> {
            var old = hasChatChannel.getChatChannel();
            try {
                hasChatChannel.setChatChannel(ChatChannel.ALL);
                server.getPlayerList().broadcastChatMessage(message, source, ChatType.bind(ChatType.CHAT, source));
            } finally {
                hasChatChannel.setChatChannel(old);
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
