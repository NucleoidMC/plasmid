package xyz.nucleoid.plasmid.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import xyz.nucleoid.plasmid.api.chat.ChatChannel;
import xyz.nucleoid.plasmid.api.chat.HasChatChannel;

import static net.minecraft.commands.Commands.literal;

public class ChatCommand {
    // @formatter:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("chat")
                .then(literal("all").executes(ChatCommand::switchToAll))
                .then(literal("team").executes(ChatCommand::switchToTeam))
        );
    }
    // @formatter:on

    public static int switchToAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        ((HasChatChannel) player).setChatChannel(ChatChannel.ALL);
        player.sendSystemMessage(Component.translatable("text.plasmid.chat.switch.all").withStyle(ChatFormatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int switchToTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        ((HasChatChannel) player).setChatChannel(ChatChannel.TEAM);
        player.sendSystemMessage(Component.translatable("text.plasmid.chat.switch.team").withStyle(ChatFormatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }
}
