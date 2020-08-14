package xyz.nucleoid.plasmid.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.chat.ChatChannel;
import xyz.nucleoid.plasmid.chat.HasChatChannel;

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
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ((HasChatChannel) player).setChatChannel(ChatChannel.ALL);
        player.sendMessage(new LiteralText("Chat channel switched to all.").formatted(Formatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int switchToTeam(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ((HasChatChannel) player).setChatChannel(ChatChannel.ALL);
        player.sendMessage(new LiteralText("Chat channel switched to team.").formatted(Formatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }
}
