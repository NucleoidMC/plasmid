package xyz.nucleoid.plasmid.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.UserCache;
import xyz.nucleoid.plasmid.storage.FriendList;
import xyz.nucleoid.plasmid.storage.FriendListManager;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FriendCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("friend")
                .then(literal("accept").executes(FriendCommand::acceptFriend))
                .then(literal("add").then(argument("playerName", GameProfileArgumentType.gameProfile()).executes(FriendCommand::registerFriendAdd)))
                .then(literal("list").executes(FriendCommand::ListFriends)));
    }


    private static int ListFriends(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getMinecraftServer();
        ServerPlayerEntity player = context.getSource().getPlayer();
        UserCache uCache = server.getUserCache();

        if (FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid()).returnFlistIds().size() <= 0) {
            player.sendMessage(new LiteralText("You have no friends..."), false);
            return Command.SINGLE_SUCCESS;
        }

        int i = 0;
        FriendList f = FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid());
        System.out.println("Listing friends");

        for (UUID ids : f.returnFlistIds()) {
            i++;
            player.sendMessage(new LiteralText("[" + i + "] " + uCache.getByUuid(ids).getName()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int acceptFriend(CommandContext<ServerCommandSource> context) {
        System.out.println("accepting");
        return Command.SINGLE_SUCCESS;
    }

    private static int registerFriendAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (GameProfile Profile : GameProfileArgumentType.getProfileArgument(context, "playerName")) {
            if (Profile.equals(context.getSource().getPlayer().getGameProfile())) {
                context.getSource().getPlayer().sendMessage(new LiteralText("You can't friend yourself silly!"), false);
                return Command.SINGLE_SUCCESS;
            }
            System.out.println(Profile.getName());
            System.out.println(FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid()));

            FriendListManager.returnFriendlist(context.getSource().getPlayer().getUuid()).addFreind(Profile.getId());
            System.out.println("Added friend");
        }

        return Command.SINGLE_SUCCESS;
    }
}
