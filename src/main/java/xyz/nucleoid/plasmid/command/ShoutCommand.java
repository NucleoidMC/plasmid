package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShoutCommand {
    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("shout")
                .then(
                    argument("message", StringArgumentType.greedyString()).executes(ShoutCommand::sendMessage)
                )
        );
    }
    // @formatter:on

    public static int sendMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        HasChatChannel hasChatChannel = (HasChatChannel) player;
        ChatChannel old = hasChatChannel.getChatChannel();
        hasChatChannel.setChatChannel(ChatChannel.ALL);

        String message = StringArgumentType.getString(context, "message");
        context.getSource().getServer().getPlayerManager().broadcastChatMessage(
                new TranslatableText("chat.type.text", player.getDisplayName(), message),
                MessageType.CHAT,
                context.getSource().getPlayer().getUuid()
        );

        hasChatChannel.setChatChannel(old);
        return Command.SINGLE_SUCCESS;
    }
}
