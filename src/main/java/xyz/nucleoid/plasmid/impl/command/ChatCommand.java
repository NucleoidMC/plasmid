package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.chat.ChatChannel;
import xyz.nucleoid.plasmid.api.chat.HasChatChannel;

import static net.minecraft.server.command.CommandManager.literal;

public class ChatCommand {
    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("chat")
                .then(literal("all").executes(ChatCommand::switchToAll))
                .then(literal("team").executes(ChatCommand::switchToTeam))
        );
    }
    // @formatter:on

    public static int switchToAll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrThrow();
        ((HasChatChannel) player).setChatChannel(ChatChannel.ALL);
        player.sendMessage(Text.translatable("text.plasmid.chat.switch.all").formatted(Formatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int switchToTeam(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrThrow();
        ((HasChatChannel) player).setChatChannel(ChatChannel.TEAM);
        player.sendMessage(Text.translatable("text.plasmid.chat.switch.team").formatted(Formatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }
}
